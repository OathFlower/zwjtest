package cn.xunhou.xbbcloud.sched;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.common.tools.util.SpringContextUtil;
import cn.xunhou.grpc.proto.asset.AssetXhServerGrpc;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryDetailRepository;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantFlowRepository;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantInfoRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantFlowEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.*;
import cn.xunhou.xbbcloud.rpc.salary.pojo.result.SalaryDetailResult;
import cn.xunhou.xbbcloud.rpc.salary.service.SalaryService;
import com.google.common.collect.Maps;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@Component
public class SalarySched {
    @GrpcClient("ins-assetxh-platform")
    private AssetXhServerGrpc.AssetXhServerBlockingStub assetXhServerBlockingStub;

    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;

    @Resource
    private SalaryMerchantFlowRepository salaryMerchantFlowRepository;


    @Resource
    private SalaryDetailRepository salaryDetailRepository;
    @Resource
    private SalaryService salaryService;

    /**
     * 拉取充值流水
     *
     * @param param 参数
     * @return 查询结果
     */
    @XxlJob("salary-recharge-flow")
    public ReturnT<String> salaryRechargeFlow(String param) {
        SalaryRechargeFlowParam salaryRechargeFlowParam;
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }
        if (CharSequenceUtil.isBlank(param)) {
            salaryRechargeFlowParam = new SalaryRechargeFlowParam().setDateOffset(0).setIds(Collections.emptyList());
        } else {
            salaryRechargeFlowParam = JSONUtil.toBean(param, SalaryRechargeFlowParam.class);
            if (salaryRechargeFlowParam.getDateOffset() > 0) {
                throw ExceptionUtil.wrapRuntime("偏移日期请填小于等于0");
            }
        }
        List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo().setTenantTypes(Collections.singletonList(SalaryServerProto.EnumTenantType.SAAS_VALUE)));
        log.info("salaryMerchantInfoEntityList = {} ", JSONUtil.toJsonStr(salaryMerchantInfoEntityList));
        for (SalaryMerchantInfoEntity entity : salaryMerchantInfoEntityList) {
            if (CharSequenceUtil.isNotBlank(param)) {
                List<Long> ids = salaryRechargeFlowParam.getIds();
                if (CollUtil.isNotEmpty(ids) && !ids.contains(entity.getId())) {
                    continue;
                }
            }
            try {
                log.info("SalaryMerchantInfoEntity = {} ;salaryRechargeFlowParam = {}", JSONUtil.toJsonStr(entity), JSONUtil.toJsonStr(salaryRechargeFlowParam));
                SpringContextUtil.getBean(SalarySched.class).wxMerchantFlow(entity, salaryRechargeFlowParam);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(String.format("拉取充值流水异常 MerchantInfo = %s", entity), e);
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 支付处理中（未认证的状态）两天后关闭
     *
     * @param param 参数
     * @return 查询结果
     */
    @XxlJob("salary-overtime")
    public ReturnT<String> salaryOvertime(String param) {
        SalaryOvertimeParam salaryOvertimeParam;
        if (CharSequenceUtil.isBlank(param)) {
            param = XxlJobHelper.getJobParam();
        }
        if (CharSequenceUtil.isBlank(param)) {
            salaryOvertimeParam = new SalaryOvertimeParam().setDateOffset(-2).setBatchId(null);
        } else {
            salaryOvertimeParam = JSONUtil.toBean(param, SalaryOvertimeParam.class);

        }

        //查询两天前的SalaryList
        SalaryDetailPageParam salaryDetailPageParam = new SalaryDetailPageParam();
        salaryDetailPageParam.setPage(null);
        salaryDetailPageParam.setDetailStatus(Collections.singletonList(SalaryServerProto.EnumSalaryDetailStatus.PAYING_NOT_AUTH.getNumber()));
        if (salaryOvertimeParam.getBatchId() != null) {
            salaryDetailPageParam.setBatchId(salaryOvertimeParam.getBatchId());
        }
        salaryDetailPageParam.setEndSubmitTime(DateUtil.offsetDay(DateUtil.date(), salaryOvertimeParam.getDateOffset()).toTimestamp());

        log.info("salaryOvertime查询入参 = {} ", JSONUtil.toJsonStr(salaryDetailPageParam));
        PagePojoList<SalaryDetailResult> pagePojoList = salaryDetailRepository.findSalaryDetailPageList(salaryDetailPageParam);
        log.info("salaryOvertime查询结果 = {} ", JSONUtil.toJsonStr(pagePojoList));
        if (pagePojoList != null && CollUtil.isNotEmpty(pagePojoList.getData())) {
            List<SalaryDetailEntity> salaryDetailEntityList = new ArrayList<>();
            for (SalaryDetailResult salaryDetailResult :
                    pagePojoList.getData()) {

                SalaryDetailEntity salaryDetailEntity = new SalaryDetailEntity();
                salaryDetailEntity.setId(salaryDetailResult.getId());
                salaryDetailEntity.setStatus(SalaryServerProto.EnumSalaryDetailStatus.PAY_FAIL.getNumber());
                salaryDetailEntity.setBatchId(salaryDetailResult.getBatchId());
                salaryDetailEntity.setFailureReason("超时未认证");
                salaryDetailEntityList.add(salaryDetailEntity);
            }
            salaryService.updateDetailStatus(salaryDetailEntityList);
        }


        return ReturnT.SUCCESS;
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void wxMerchantFlow(SalaryMerchantInfoEntity wxMerchantInfo, SalaryRechargeFlowParam param) {
        AssetXhServerProto.WxMerchantRechargeFlowListResponse flowListResponse = assetXhServerBlockingStub.wxRechargeFlow(AssetXhServerProto.WxMerchantInfoRequest.newBuilder()
                .setWxMerchantId(wxMerchantInfo.getServiceMerchantNo())
                .setWxMerchantName(wxMerchantInfo.getPayeeMerchantName())
                .setWxSubMchid(wxMerchantInfo.getSpecialMerchantId())
                .setDate(DateUtil.format(DateUtil.offsetDay(DateUtil.date(), param.getDateOffset()), DatePattern.NORM_DATE_PATTERN))
                .build());
        log.info("flowListResponse = {}", XbbProtoJsonUtil.toJsonString(flowListResponse));
        if (CollUtil.isEmpty(flowListResponse.getDataList())) {
            return;
        }
        Long tenantId = wxMerchantInfo.getId();
        Map<String, SalaryMerchantFlowEntity> rechargeFlowMap = Maps.newHashMap();
        for (AssetXhServerProto.WxMerchantRechargeFlowResponse rechargeFlowResponse : flowListResponse.getDataList()) {
            rechargeFlowMap.put(rechargeFlowResponse.getIncomeRecordId(), SalaryConvert.response2Entity(rechargeFlowResponse, tenantId,
                    wxMerchantInfo.getPayeeMerchantName(), wxMerchantInfo.getSpecialMerchantId()
            ));
        }
        List<SalaryMerchantFlowEntity> existFlowEntityList = salaryMerchantFlowRepository.query(new QuerySalaryMerchantFlow().setTenantId(tenantId).setFlowNos(rechargeFlowMap.keySet()));
        if (CollUtil.isNotEmpty(existFlowEntityList)) {
            MapUtil.removeAny(rechargeFlowMap, existFlowEntityList.stream().map(SalaryMerchantFlowEntity::getFlowNo).toArray(String[]::new));
        }
        Collection<SalaryMerchantFlowEntity> insetList = rechargeFlowMap.values();
        if (CollUtil.isNotEmpty(insetList)) {
            salaryMerchantFlowRepository.batchInsert(insetList);
        }
    }

    /**
     * 套餐有效期
     *
     * @param param
     * @return
     */
    @XxlJob("use-to-date")
    public ReturnT<String> useToDate(String param) {
        List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo());


        for (SalaryMerchantInfoEntity salaryMerchantInfo : salaryMerchantInfoEntityList) {

        }

        return ReturnT.SUCCESS;
    }


}
