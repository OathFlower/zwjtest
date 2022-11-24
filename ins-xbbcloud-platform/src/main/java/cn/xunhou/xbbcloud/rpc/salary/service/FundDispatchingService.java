package cn.xunhou.xbbcloud.rpc.salary.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.snow.SnowflakeIdGenerator;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.asset.AssetXhServerGrpc;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.hrm.HrmServiceGrpc;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.enums.EnumCapitalType;
import cn.xunhou.xbbcloud.common.enums.EnumDispatchStatus;
import cn.xunhou.xbbcloud.common.enums.IEnum;
import cn.xunhou.xbbcloud.common.exception.GrpcException;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.salary.dao.FundDispatchingRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.handler.event.FundDispatchingEvent;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QueryFundDispatchingParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 资金调度服务
 *
 * @author wangkm
 */
@Slf4j
@Service
public class FundDispatchingService {
    @GrpcClient("ins-assetxh-platform")
    private AssetXhServerGrpc.AssetXhServerBlockingStub assetXhServerBlockingStub;

    @GrpcClient("ins-assetxh-platform")
    private AssetXhServerGrpc.AssetXhServerStub assetXhServerStub;

    @Setter(onMethod = @__(@GrpcClient("ins-xhportal-platform")))
    private HrmServiceGrpc.HrmServiceBlockingStub hrmServiceBlockingStub;

    /**
     * 注入事件发布者
     */
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private FundDispatchingRepository fundDispatchingRepository;


    /**
     * TODO 固化商户交易信息
     * 资金调度
     * 1. 代发类型交易 <br/>
     * 2. 判断子账户余额 <br/>
     * 2.1 余额不足，中断交易 <br/>
     * 2.2 余额充足，后续交易 <br/>
     * 3. 形成调度节点（实发调度，服务费、税费 数据入库） <br/>
     * 4. 发起资金调度流程（等待资金调度节点状态返回） <br/>
     * 5. 接收资金调度节点MQ <br/>
     * 6. 更新资金调度节点状态（更新资金流程状态） <br/>
     * 6.1 交易节点失败，中断后续交易节点（推送交易失败事件） <br/>
     * 6.2 交易节点成功，等待资金流程完成（推送交易成功事件） <br/>
     * 7. 资金调度完成 （后续使用微信，发薪到用户微信零钱） <br/>
     *
     * @param salaryMerchantInfo 交易商户信息
     * @param transactionMain    调度流程主键（可使用批次id，标识当前动作下所有交易），后续可使用这个查询调度结果
     * @param f                  总服务费(分)
     * @param s                  总税费(分)
     * @param a                  总实发金额(分)
     * @param operatorId         操作人
     */
    public void fundDispatching(SalaryMerchantInfoEntity salaryMerchantInfo, String transactionMain, BigDecimal f, BigDecimal s, BigDecimal a, Long operatorId) {
        log.info("开始调度 SalaryMerchantInfoEntity={},transactionMain={},f={},s={},a={},operatorId={}", JSONUtil.toJsonStr(salaryMerchantInfo), transactionMain, f, s, a, operatorId);
        if (salaryMerchantInfo.getTenantType() != SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE) {
            throw GrpcException.asRuntimeException("非代发租户！");
        }
        List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingRepository.query(new QueryFundDispatchingParam().setTransactionMain(transactionMain));
        if (CollUtil.isNotEmpty(fundDispatchingEntityList)) {
            throw GrpcException.asRuntimeException("已存在调度数据，不能重复发起！");
        }
        checkAmount(f, s, a, salaryMerchantInfo.getPayeeSubAccountId());

        SpringContextUtil.getBean(FundDispatchingService.class).generateFundDispatchingList(salaryMerchantInfo, transactionMain, f, s, a, operatorId);

        executeFundDispatch(fundDispatchingRepository.query(new QueryFundDispatchingParam().setTransactionMain(transactionMain)), operatorId);
    }


    public void retryFundDispatchStatus(Collection<FundDispatchingEntity> fundDispatchingEntityList) {
        fundDispatchingRepository.updateRetryStatus(fundDispatchingEntityList.stream().map(FundDispatchingEntity::getId).collect(Collectors.toList()));
    }

    /**
     * 执行调度<br/>
     * 执行一个批次的 税费|服务费|实发金额 <br/>
     * 通过所有节点，根据节点状态分组，分别发送税费，服务费，实发金额 <br/>
     * 执行重试，需将重试次数+1 <br/>
     *
     * @param fundDispatchingList 当前批次的调度节点
     */
    public void executeFundDispatch(List<FundDispatchingEntity> fundDispatchingList, Long operator) {
        log.info("执行调度 fundDispatchingList={}", JSONUtil.toJsonStr(fundDispatchingList));

        Map<Integer, List<FundDispatchingEntity>> groupList = fundDispatchingList.parallelStream().collect(Collectors.groupingBy(FundDispatchingEntity::getCapitalType));
        boolean fail = false;
        for (Integer key : groupList.keySet()) {
            List<FundDispatchingEntity> fundDispatchingEntityList = groupList.get(key);
            List<Long> ids = fundDispatchingEntityList.stream().map(FundDispatchingEntity::getId).collect(Collectors.toList());
            List<AssetXhServerProto.FundDispatchingRequest.DispatchingNode> nodes = generateNodes(fundDispatchingEntityList);
            log.info("调度节点 nodes={}", nodes);
            if (CollUtil.isNotEmpty(nodes)) {
                AssetXhServerProto.FundDispatchingRequest.Builder builder = AssetXhServerProto.FundDispatchingRequest.newBuilder();
                builder.addAllDispatchingNodes(nodes);
                builder.setSystemPayType(AssetXhServerProto.EnumSystemPayType.SP_XCY_DF_FUND_DISPATCHING);
                builder.setCurrentOrder(CollUtil.getFirst(nodes).getOrder());
                builder.setOperator(operator);
                try {
                    //交易中
                    fundDispatchingRepository.updateState(ids, EnumDispatchStatus.PROCESSING, null, null);
                    AssetXhServerProto.TransferResponse transferResponse = assetXhServerBlockingStub.fundDispatching(builder.build());
                    if (transferResponse.getState() != AssetXhServerProto.EnumTransferResponse.TRANSFER_SUCCESS) {
                        fail = true;
                        fundDispatchingRepository.updateState(ids, EnumDispatchStatus.FAILURE, CharSequenceUtil.sub(transferResponse.getMessage(), 0, 400), transferResponse.getAssetTransactionId());
                    } else {
                        fundDispatchingRepository.updateState(ids, EnumDispatchStatus.PROCESSING, null, transferResponse.getAssetTransactionId());
                    }
                } catch (Exception e) {
                    fail = true;
                    fundDispatchingRepository.updateState(ids, EnumDispatchStatus.FAILURE, CharSequenceUtil.sub(e.getMessage(), 0, 400), null);
                }
            }

        }
        if (fail) {
            pushFundDispatchProcessResult(CollUtil.getFirst(fundDispatchingList).getTransactionMain());
        }
    }

    /**
     * 做成请求节点
     *
     * @param fundDispatchingEntityList
     * @return
     */
    private List<AssetXhServerProto.FundDispatchingRequest.DispatchingNode> generateNodes(List<FundDispatchingEntity> fundDispatchingEntityList) {
        List<AssetXhServerProto.FundDispatchingRequest.DispatchingNode> nodes = Lists.newArrayList();
        for (FundDispatchingEntity fundDispatching : fundDispatchingEntityList) {
            EnumDispatchStatus dispatchStatus = IEnum.formCode(EnumDispatchStatus.class, fundDispatching.getDispatchStatus());
            if (dispatchStatus != null) {
                switch (dispatchStatus) {
                    case INIT:
                    case FAILURE:
                        nodes.add(SalaryConvert.entity2Request(fundDispatching).build());
                        break;
                    default:
                        break;
                }

            }
        }
        nodes.sort(Comparator.comparing(AssetXhServerProto.FundDispatchingRequest.DispatchingNode::getOrder));
        return nodes;
    }


    /**
     * 做成交易调度节点
     * 1. 服务费
     * 2. 税费
     * 3. 调度节点（收款账户->出款账户->商户号）
     *
     * @param transactionMain 调度流程主键（可使用批次id，标识当前动作下所有交易），后续可使用这个查询调度结果
     * @param f               服务费(分)
     * @param s               税费(分)
     * @param a               实发金额(分)
     * @param operatorId      操作人
     * @return 做成的调度节点
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void generateFundDispatchingList(SalaryMerchantInfoEntity salaryMerchantInfo, String transactionMain, BigDecimal f, BigDecimal s, BigDecimal a, Long operatorId) {
        List<FundDispatchingEntity> list = Lists.newArrayList();

        Long tenantId = salaryMerchantInfo.getId();
        Long payer = salaryMerchantInfo.getPayeeSubAccountId();
        Long payee = -1L;

        HrmServiceProto.TenantBeResponses tenantBeResponses = hrmServiceBlockingStub.withDeadlineAfter(10, TimeUnit.SECONDS).findTenant(HrmServiceProto.TenantRequest.newBuilder().addId(salaryMerchantInfo.getId().intValue()).build());
        HrmServiceProto.TenantBeResponse tenantBeResponse = CollUtil.getFirst(tenantBeResponses.getDataList());
        String company = tenantBeResponse.getCompany();

        FundDispatchingEntity fFundDispatching = generateFundDispatchingEntity(tenantId, company, f, transactionMain, payer, payee, operatorId, EnumCapitalType.SERVICE_CHARGE, AssetXhServerProto.EnumBusinessType.B2B);
        FundDispatchingEntity sFundDispatching = generateFundDispatchingEntity(tenantId, company, s, transactionMain, payer, payee, operatorId, EnumCapitalType.TAXES, AssetXhServerProto.EnumBusinessType.B2B);
        list.add(fFundDispatching.setOrderValue(0));
        list.add(sFundDispatching.setOrderValue(0));

        List<AssetXhServerProto.SubAccountResponse> subAccountInfosList = assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                .setPageable(false)
                .setSubAccountType(AssetXhServerProto.EnumSubAccountType.CM_BANK)
                .addSubAccountNos("000001")
                .addSubjectInfoIds(salaryMerchantInfo.getPayerSubjectId())
                .build()).getSubAccountInfosList();
        //实发调度流程 B2B -> B2WX
        AssetXhServerProto.SubAccountResponse subAccountResponse = CollUtil.getFirst(subAccountInfosList);
        payee = subAccountResponse.getSubAccountId();

        FundDispatchingEntity fd1 = generateFundDispatchingEntity(tenantId, company, a, transactionMain, payer, payee, operatorId, EnumCapitalType.PAID_AMOUNT, AssetXhServerProto.EnumBusinessType.B2B);
        fd1.setId(SnowflakeIdGenerator.getId());
        FundDispatchingEntity fd2 = generateFundDispatchingEntity(tenantId, company, a, transactionMain, payee, payee, operatorId, EnumCapitalType.PAID_AMOUNT, AssetXhServerProto.EnumBusinessType.B2WX);
        fd2.setParentId(fd1.getId());
        list.add(fd1.setOrderValue(0));
        list.add(fd2.setOrderValue(1));
        log.info("成功做成调度节点 fundDispatchingList={}", JSONUtil.toJsonStr(list));

        fundDispatchingRepository.insertBatch(list, transactionMain);
    }


    /**
     * 做成税费|服务费|实发金额
     *
     * @param tenantId
     * @param customerName
     * @param amount
     * @param transactionMain
     * @param payer
     * @param payee
     * @param operatorId
     * @param capitalType
     * @return
     */
    private FundDispatchingEntity generateFundDispatchingEntity(Long tenantId, String customerName, BigDecimal amount,
                                                                String transactionMain, Long payer, Long payee, Long operatorId, EnumCapitalType capitalType,
                                                                AssetXhServerProto.EnumBusinessType businessType
    ) {
        return generateFundDispatchingEntity(tenantId, customerName, amount, transactionMain, payer, payee, operatorId, capitalType, businessType, false);
    }

    /**
     * 做成税费|服务费|实发金额
     *
     * @param tenantId
     * @param customerName
     * @param amount
     * @param transactionMain
     * @param payer
     * @param payee
     * @param operatorId
     * @param capitalType
     * @return
     */
    private FundDispatchingEntity generateFundDispatchingEntity(Long tenantId, String customerName, BigDecimal amount,
                                                                String transactionMain, Long payer, Long payee, Long operatorId, EnumCapitalType capitalType,
                                                                AssetXhServerProto.EnumBusinessType businessType, Boolean back
    ) {
        String s = capitalType.getMessage();
        String remark = CharSequenceUtil.sub(customerName, 0, 5) + s;
        EnumDispatchStatus dispatchStatus;
        if (BigDecimal.ZERO.compareTo(amount) == 0) {
            dispatchStatus = EnumDispatchStatus.SUCCESSFUL;
        } else {
            if (capitalType == EnumCapitalType.BACK_TAXES || capitalType == EnumCapitalType.BACK_SERVICE_CHARGE) {
                dispatchStatus = EnumDispatchStatus.PENDING;
            } else {
                dispatchStatus = EnumDispatchStatus.INIT;
            }
        }
        FundDispatchingEntity fFundDispatching = new FundDispatchingEntity();
        fFundDispatching.setTenantId(tenantId);
        fFundDispatching.setTransactionMain(transactionMain);
        fFundDispatching.setCapitalType(capitalType.getCode());
        fFundDispatching.setDispatchStatus(dispatchStatus.getCode());
        fFundDispatching.setOperatorId(operatorId);
        fFundDispatching.setAmount(amount.intValue());
        fFundDispatching.setPayerId(payer);
        fFundDispatching.setPayeeId(payee);
        fFundDispatching.setRemark1(remark);
        fFundDispatching.setRemark2(remark);
        fFundDispatching.setSourceType(0);
        fFundDispatching.setRetryCount(CommonConst.ZERO);
        fFundDispatching.setDispatchDirection(businessType.getNumber());
        return fFundDispatching;
    }


    public void checkAmount(BigDecimal fAmount, BigDecimal sAmount, BigDecimal aAmount, Long payeeSubAccountId) {
        if (BigDecimal.ZERO.compareTo(fAmount) > 0 || BigDecimal.ZERO.compareTo(sAmount) > 0 || BigDecimal.ZERO.compareTo(aAmount) >= 0) {
            throw GrpcException.asRuntimeException("金额异常！");
        }

        AssetXhServerProto.ThirdPartyAccountsResponse thirdPartyAccountsResponse = assetXhServerBlockingStub.queryThirdPartyAccountBalance(AssetXhServerProto.ThirdPartyAccountsRequest.newBuilder()
                .addThirdPartyAccounts(AssetXhServerProto.ThirdPartyAccountRequest.newBuilder()
                        .setSubAccountId(payeeSubAccountId)
                        .build())
                .build());
        AssetXhServerProto.ThirdPartyAccountResponse subAccountResponse = CollUtil.getFirst(thirdPartyAccountsResponse.getThirdPartyAccountResponsesList());
        if (NumberUtil.add(fAmount, sAmount, aAmount).compareTo(NumberUtil.mul(subAccountResponse.getBalance(), "100")) > 0) {
            throw GrpcException.asRuntimeException("余额不足！");
        }
    }


    public void pushFundDispatchProcessResult(String transactionMain) {
        FundDispatchingEvent event = getFundDispatchingEvent(transactionMain);
        handlerPendingFundDispatching(event.getFundDispatchingEntityList());
        if (Boolean.TRUE.equals(event.getFundDispatchingEnd())) {
            applicationEventPublisher.publishEvent(event);
        }
    }

    public void handlerPendingFundDispatching(List<FundDispatchingEntity> fundDispatchingEntityList) {
        List<FundDispatchingEntity> pendingFundDispatchingEntityList = Lists.newArrayList();
        Long operator = null;
        boolean fail = false;
        for (FundDispatchingEntity fundDispatching : fundDispatchingEntityList) {
            if (EnumDispatchStatus.PENDING.getCode().equals(fundDispatching.getDispatchStatus())) {
                fundDispatching.setDispatchStatus(EnumDispatchStatus.INIT.getCode());
                pendingFundDispatchingEntityList.add(fundDispatching);
                operator = fundDispatching.getOperatorId();
            }
            if (EnumDispatchStatus.FAILURE.getCode().equals(fundDispatching.getDispatchStatus())) {
                fail = true;
            }
        }
        if (CollUtil.isNotEmpty(pendingFundDispatchingEntityList) && !fail) {
            executeFundDispatch(pendingFundDispatchingEntityList, operator);
        }
    }

    /**
     * 查询调度状态，为空则还在调度中
     *
     * @param transactionMain
     * @return 调度事件
     */
    public FundDispatchingEvent getFundDispatchingEvent(String transactionMain) {
        FundDispatchingEvent event = null;
        List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingRepository.query(new QueryFundDispatchingParam().setTransactionMain(transactionMain));
        Set<EnumDispatchStatus> statusSet = Sets.newHashSet();
        String failureReason = null;
        Long tenantId = null;
        for (FundDispatchingEntity entity : fundDispatchingEntityList) {
            tenantId = entity.getTenantId();
            EnumDispatchStatus dispatchStatus = IEnum.formCode(EnumDispatchStatus.class, entity.getDispatchStatus());
            statusSet.add(dispatchStatus);
            if (dispatchStatus == EnumDispatchStatus.FAILURE && CharSequenceUtil.isNotBlank(entity.getFailureReason())) {
                failureReason = entity.getFailureReason();
            }
        }
        event = new FundDispatchingEvent(this, tenantId, null, transactionMain, null, fundDispatchingEntityList, false);
        if (statusSet.contains(EnumDispatchStatus.FAILURE)) {
            event = new FundDispatchingEvent(this, tenantId, false, transactionMain, failureReason, fundDispatchingEntityList, true);
        }

        if (statusSet.size() == CommonConst.ONE && CollUtil.getFirst(statusSet) == EnumDispatchStatus.SUCCESSFUL) {
            event = new FundDispatchingEvent(this, tenantId, true, transactionMain, null, fundDispatchingEntityList, true);
        }
        return event;
    }


    /**
     * 调度资金撤回
     * 入参撤回 用户资金
     * 校验（金额，状态）
     * 原子锁
     * 作成资金撤回流程节点
     * 形成撤回流程
     * 执行撤回交易动作
     * 释放原子锁
     * 接收处理交易结果消息
     * 推送资金撤回完成事件
     *
     * @param transactionMain 调度流程主键（可使用批次id，标识当前动作下所有交易），后续可使用这个查询调度结果
     * @param f               总服务费(分)
     * @param s               总税费(分)
     * @param a               总实发金额(分)
     * @param operatorId      操作人
     */
    public void fundDispatchingBack(SalaryMerchantInfoEntity salaryMerchantInfo, String transactionMain, BigDecimal f, BigDecimal s, BigDecimal a, Long operatorId) {
        log.info("开始进行撤回调度 SalaryMerchantInfoEntity={},transactionMain={},f={},s={},a={},operatorId={}", JSONUtil.toJsonStr(salaryMerchantInfo), transactionMain, f, s, a, operatorId);
        if (salaryMerchantInfo.getTenantType() != SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE) {
            throw GrpcException.asRuntimeException("非代发租户！");
        }
        List<FundDispatchingEntity> fundDispatchingEntityList = fundDispatchingRepository.query(new QueryFundDispatchingParam().setTransactionMain(transactionMain));
        if (CollUtil.isNotEmpty(fundDispatchingEntityList)) {
            throw GrpcException.asRuntimeException("已存在调度数据，不能重复发起！");
        }

        SpringContextUtil.getBean(FundDispatchingService.class).generateFundDispatchingBackList(salaryMerchantInfo, transactionMain, f, s, a, operatorId);

        executeFundDispatch(fundDispatchingRepository.query(new QueryFundDispatchingParam().setTransactionMain(transactionMain)), operatorId);
    }

    /**
     * 做成资金撤回流程
     *
     * @param salaryMerchantInfo
     * @param transactionMain
     * @param f
     * @param s
     * @param a
     * @param operatorId
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void generateFundDispatchingBackList(SalaryMerchantInfoEntity salaryMerchantInfo, String transactionMain, BigDecimal f, BigDecimal s, BigDecimal a, Long operatorId) {
        List<FundDispatchingEntity> list = Lists.newArrayList();
        Long tenantId = salaryMerchantInfo.getId();
        Long payer = -1L;
        Long payee = salaryMerchantInfo.getPayeeSubAccountId();
        HrmServiceProto.TenantBeResponses tenantBeResponses = hrmServiceBlockingStub.withDeadlineAfter(10, TimeUnit.SECONDS).findTenant(HrmServiceProto.TenantRequest.newBuilder().addId(salaryMerchantInfo.getId().intValue()).build());
        HrmServiceProto.TenantBeResponse tenantBeResponse = CollUtil.getFirst(tenantBeResponses.getDataList());
        String company = tenantBeResponse.getCompany();
        //将主账户余额移入子账户
        FundDispatchingEntity fFd = generateFundDispatchingEntity(tenantId, company, f, transactionMain, payer, payee, operatorId, EnumCapitalType.BACK_SERVICE_CHARGE, AssetXhServerProto.EnumBusinessType.B2B, true);
        FundDispatchingEntity sFd = generateFundDispatchingEntity(tenantId, company, s, transactionMain, payer, payee, operatorId, EnumCapitalType.BACK_TAXES, AssetXhServerProto.EnumBusinessType.B2B, true);
        fFd.setOrderValue(0);
        sFd.setOrderValue(0);
        list.add(fFd);
        list.add(sFd);

        List<AssetXhServerProto.SubAccountResponse> subAccountInfosList = assetXhServerBlockingStub.querySubAccount(AssetXhServerProto.SubAccountQueryRequest.newBuilder()
                .setPageable(false)
                .setSubAccountType(AssetXhServerProto.EnumSubAccountType.CM_BANK)
                .addSubAccountNos("000001")
                .addSubjectInfoIds(salaryMerchantInfo.getPayerSubjectId())
                .build()).getSubAccountInfosList();
        //实发调度流程 B2WX -> B2B
        AssetXhServerProto.SubAccountResponse subAccountResponse = CollUtil.getFirst(subAccountInfosList);
        payer = subAccountResponse.getSubAccountId();

        //微信转账到主账户
        FundDispatchingEntity fd1 = generateFundDispatchingEntity(tenantId, company, a, transactionMain, payer, payer, operatorId, EnumCapitalType.BACK_PAID_AMOUNT, AssetXhServerProto.EnumBusinessType.WX2B, true);
        //主账户转账到调度账户
        FundDispatchingEntity fd2 = generateFundDispatchingEntity(tenantId, company, a, transactionMain, payer, payee, operatorId, EnumCapitalType.BACK_PAID_AMOUNT, AssetXhServerProto.EnumBusinessType.B2B, true);
        fd1.setId(SnowflakeIdGenerator.getId());
        fd2.setParentId(fd1.getId());
        list.add(fd1.setOrderValue(0));
        list.add(fd2.setOrderValue(1));
        log.info("成功做成撤回调度节点 fundDispatchingList={}", JSONUtil.toJsonStr(list));
        fundDispatchingRepository.insertBatch(list, transactionMain);
    }

}
