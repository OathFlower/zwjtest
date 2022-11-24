package cn.xunhou.xbbcloud.rpc.salary.handler.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.subject.SubjectServiceGrpc;
import cn.xunhou.grpc.proto.subject.SubjectServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.enums.EnumCapitalType;
import cn.xunhou.xbbcloud.common.enums.EnumSalaryBatchStatus;
import cn.xunhou.xbbcloud.common.helper.NotifyHelper;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryBatchRepository;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryDetailRepository;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantInfoRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryBatchEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.handler.event.FundDispatchingEvent;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.SalaryDetailQueryListParam;
import cn.xunhou.xbbcloud.rpc.salary.service.SalaryService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 调度，发薪详情逻辑分离，事件传输交易状态
 *
 * @author wangkm
 */
@Slf4j
@Component
public class FundDispatchingEventListener implements ApplicationListener<FundDispatchingEvent> {
    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;
    @Resource
    private SalaryDetailRepository salaryDetailRepository;
    @Resource
    private SalaryBatchRepository salaryBatchRepository;

    @Resource
    private SalaryService salaryService;
    @GrpcClient("ins-xhportal-platform")
    private SubjectServiceGrpc.SubjectServiceBlockingStub subjectServiceBlockingStub;

    /**
     * @param fundDispatchingEvent
     */
    @Override
    public void onApplicationEvent(@NotNull FundDispatchingEvent fundDispatchingEvent) {
        FundDispatchingEntity fundDispatching = CollUtil.getFirst(fundDispatchingEvent.getFundDispatchingEntityList());
        if (Lists.newArrayList(
                EnumCapitalType.BACK_PAID_AMOUNT.getCode(),
                EnumCapitalType.BACK_TAXES.getCode(),
                EnumCapitalType.BACK_SERVICE_CHARGE.getCode()
        ).contains(fundDispatching.getCapitalType())) {
            if (Boolean.TRUE.equals(fundDispatchingEvent.getDispatchSuccess())) {
                backSuccess(fundDispatchingEvent);
            } else {
                backFail(fundDispatchingEvent);
            }
        } else {
            if (Boolean.TRUE.equals(fundDispatchingEvent.getDispatchSuccess())) {
                success(fundDispatchingEvent);
            } else {
                fail(fundDispatchingEvent);
            }
        }

    }

    /**
     * 撤回成功
     */
    private void backSuccess(FundDispatchingEvent fundDispatchingEvent) {
//        "BACK" + CommonConst.UNDERLINE + detailId
        Long detailId = Long.valueOf(CollUtil.getLast(CharSequenceUtil.split(fundDispatchingEvent.getTransactionMain(), CommonConst.UNDERLINE)));
        // 更新详情状态  已撤回
        List<SalaryDetailEntity> salaryDetailEntityList = new ArrayList<>();
        SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
        salaryDetailEntity.setId(detailId);
        salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.CANCELLED.getNumber());
        salaryDetailEntityList.add(salaryDetailEntity);

        if (CollectionUtil.isNotEmpty(salaryDetailEntityList)) {
            SalaryDetailEntity byId = salaryDetailRepository.findById(salaryDetailEntityList.get(0).getId(), SalaryDetailEntity.class);
            for (SalaryDetailEntity detailEntity :
                    salaryDetailEntityList) {
                detailEntity.setBatchId(byId.getBatchId());
            }
            salaryService.updateDetailStatus(salaryDetailEntityList);
        }

    }

    /**
     * 撤回失败
     *
     * @param fundDispatchingEvent
     */
    private void backFail(FundDispatchingEvent fundDispatchingEvent) {
        // 接钉钉报警
        SpringContextUtil.getBean(NotifyHelper.class).sendDdMessage(String.format("资金撤回失败：%s", JSONUtil.toJsonStr(fundDispatchingEvent)));
        Long detailId = Long.valueOf(CollUtil.getLast(CharSequenceUtil.split(fundDispatchingEvent.getTransactionMain(), CommonConst.UNDERLINE)));
        //更新明细状态
        SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
        salaryDetailEntity.setUpdatedAt(null);
        salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.CANCEL_FAILED.getNumber());
        salaryDetailRepository.updateById(detailId, salaryDetailEntity);

    }

    /**
     * 调度成功
     * 1. 判断是否是发送到xbb余额
     * &nbsp;&nbsp;1.1 发送到用户xbb余额，等待用户提现到微信零钱，xbb提现逻辑
     * &nbsp;&nbsp;1.2 发送到用户微信零钱，走微信转账逻辑
     * 2. 更新交易批次，详情状态
     *
     * @param fundDispatchingEvent
     */
    private void success(FundDispatchingEvent fundDispatchingEvent) {
        log.info(String.format("调度成功：%s", fundDispatchingEvent.getTransactionMain()));
        SalaryMerchantInfoEntity salaryMerchantInfoEntity = salaryMerchantInfoRepository.findById(fundDispatchingEvent.getTenantId());
        //代发客户的微信商户信息
        Long subjectId = salaryMerchantInfoEntity.getPayerSubjectId();
        SubjectServiceProto.SubjectDetailBeResponse subjectObjectById = subjectServiceBlockingStub.getSubjectObjectById(SubjectServiceProto.IdBeRequest.newBuilder().setId(subjectId).build());
        //特约商户id
        String specialMerchantId = subjectObjectById.getMerchantAccount();
        String payeeMerchantName = subjectObjectById.getSubjectName();
        String payeeMerchantNo = subjectObjectById.getWxCollectionBankCardNum();


        //认证通过并且有OpenId的明细
        SalaryDetailQueryListParam salaryDetailQueryListParam = new SalaryDetailQueryListParam();
        salaryDetailQueryListParam.setTenantId(fundDispatchingEvent.getTenantId());
        salaryDetailQueryListParam.setBatchId(Long.valueOf(fundDispatchingEvent.getTransactionMain()));
        List<SalaryDetailEntity> salaryDetailEntityList = salaryDetailRepository.list(salaryDetailQueryListParam);

        List<SalaryDetailEntity> detailSalaryResultList = salaryDetailEntityList.stream().filter(o -> o.getStatus().equals(SalaryServerProto.EnumSalaryDetailStatus.PAYING_ALREADY_HANDLE.getNumber())).collect(Collectors.toList());

        if (CharSequenceUtil.isNotBlank(salaryMerchantInfoEntity.getExtInfo())) {
            SalaryMerchantInfoEntity.ExtInfo extInfo = XbbJsonUtil.fromJsonString(salaryMerchantInfoEntity.getExtInfo(), SalaryMerchantInfoEntity.ExtInfo.class);
            if (extInfo.getXcxWithdraw() == CommonConst.ONE) {
                salaryService.pushWithdraw(detailSalaryResultList, specialMerchantId, payeeMerchantName, payeeMerchantNo);
            } else {
                salaryService.transferWx(fundDispatchingEvent.getTransactionMain(), payeeMerchantNo, payeeMerchantName, specialMerchantId, detailSalaryResultList);
            }

        }

    }

    /**
     * 调度失败
     * 1. 更新交易批次，详情失败
     *
     * @param fundDispatchingEvent
     */
    private void fail(FundDispatchingEvent fundDispatchingEvent) {
        log.info(String.format("调度失败：%s", fundDispatchingEvent.getTransactionMain()));
        Long batchId = Long.valueOf(fundDispatchingEvent.getTransactionMain());
        SalaryBatchEntity salaryBatchEntity = salaryBatchRepository.findById(batchId, SalaryBatchEntity.class);
        if (salaryBatchEntity == null) {
            //不处理无效交易
            return;
        }
        if (EnumSalaryBatchStatus.ALL_FAIL.getCode().equals(salaryBatchEntity.getStatus())) {
            //不处理已经批次失败的交易
            return;
        }
        //接钉钉报警
        SpringContextUtil.getBean(NotifyHelper.class).sendDdMessage(String.format("资金调度失败：%s", JSONUtil.toJsonStr(fundDispatchingEvent)));
        SalaryDetailQueryListParam salaryDetailQueryListParam = new SalaryDetailQueryListParam();
        salaryDetailQueryListParam.setTenantId(fundDispatchingEvent.getTenantId());
        salaryDetailQueryListParam.setBatchId(batchId);
        List<SalaryDetailEntity> list = salaryDetailRepository.list(salaryDetailQueryListParam);

        for (SalaryDetailEntity salaryDetailEntity :
                list) {
            salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL.getNumber());
            salaryDetailEntity.setFailureReason("调度失败");
        }
        salaryService.updateDetailStatus(list);
    }
}
