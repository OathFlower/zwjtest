package cn.xunhou.xbbcloud.middleware.rocket.consumer;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.xunhou.cloud.core.json.XbbProtoJsonUtil;
import cn.xunhou.cloud.rocketmq.AbstractXbbMessageListener;
import cn.xunhou.cloud.rocketmq.XbbCommonRocketListener;
import cn.xunhou.cloud.rocketmq.XbbMessageBuilder;
import cn.xunhou.grpc.proto.asset.AssetXhServerProto;
import cn.xunhou.grpc.proto.xbbcloud.SalaryServerProto;
import cn.xunhou.xbbcloud.common.constants.CommonConst;
import cn.xunhou.xbbcloud.common.constants.RocketConstant;
import cn.xunhou.xbbcloud.config.JdbcConfiguration;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantFlowRepository;
import cn.xunhou.xbbcloud.rpc.salary.dao.SalaryMerchantInfoRepository;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantFlowEntity;
import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryMerchantInfoEntity;
import cn.xunhou.xbbcloud.rpc.salary.pojo.SalaryConvert;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QuerySalaryMerchantFlow;
import cn.xunhou.xbbcloud.rpc.salary.pojo.param.QuerySalaryMerchantInfo;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangkm
 */
@Slf4j
@XbbCommonRocketListener(tag = RocketConstant.ASSET_CMB_BILL_FLOW_TAG, applicationName = RocketConstant.APPLICATION_NAME)
public class SalaryCmbBillFlowListener extends AbstractXbbMessageListener {

    @Resource
    private SalaryMerchantFlowRepository salaryMerchantFlowRepository;

    @Resource
    private SalaryMerchantInfoRepository salaryMerchantInfoRepository;

    @Override
    public void dispose(XbbMessageBuilder.XbbMessage xbbMessage, ConsumeContext consumeContext) throws UnsupportedEncodingException {
        log.info("{}.dispose 资金调度节点MQ", this.getClass());
        AssetXhServerProto.CmbBillFlowListResponse cmbBillFlowListResponse = (AssetXhServerProto.CmbBillFlowListResponse) XbbProtoJsonUtil.fromJsonString(AssetXhServerProto.CmbBillFlowListResponse.newBuilder(), new String(xbbMessage.getBody(), StandardCharsets.UTF_8));
        log.info("message = {}", XbbProtoJsonUtil.toJsonString(cmbBillFlowListResponse));
        handler(cmbBillFlowListResponse);
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = JdbcConfiguration.XBBCLOUD_TRANSACTION_MANAGER)
    public void handler(AssetXhServerProto.CmbBillFlowListResponse response) {

        Map<Long, List<AssetXhServerProto.CmbBillFlowResponse>> subAccountGroup = Maps.newHashMap();
        Map<String, AssetXhServerProto.CmbBillFlowResponse> rechargeFlowMap = Maps.newHashMap();
        for (AssetXhServerProto.CmbBillFlowResponse flowResponse : response.getCmbBillFlowResponseList()) {
            if (flowResponse.getSubAccountId() != CommonConst.ZERO
                    && flowResponse.getTrxdir() == AssetXhServerProto.EnumTradingDirectionType.COLLECTION_TD
            ) {
                List<AssetXhServerProto.CmbBillFlowResponse> flowResponseList = subAccountGroup.get(flowResponse.getSubAccountId());
                if (flowResponseList == null) {
                    flowResponseList = Lists.newArrayList();
                }
                flowResponseList.add(flowResponse);
                rechargeFlowMap.put(flowResponse.getTrxnbr(), flowResponse);
            }
        }
        List<SalaryMerchantInfoEntity> salaryMerchantInfoEntityList = salaryMerchantInfoRepository.query(new QuerySalaryMerchantInfo()
                .setTenantTypes(Collections.singletonList(SalaryServerProto.EnumTenantType.BEHALF_ISSUED_VALUE))
                .setPayeeSubAccountIds(subAccountGroup.keySet())
        );

        Map<Long, SalaryMerchantInfoEntity> subAccountMapperTenantId = Maps.newHashMap();
        for (SalaryMerchantInfoEntity salaryMerchantInfo : salaryMerchantInfoEntityList) {
            subAccountMapperTenantId.put(salaryMerchantInfo.getPayeeSubAccountId(), salaryMerchantInfo);
        }
        Set<Long> tenantIds = salaryMerchantInfoEntityList.stream().map(SalaryMerchantInfoEntity::getId).collect(Collectors.toSet());

        List<SalaryMerchantFlowEntity> existFlowEntityList = salaryMerchantFlowRepository.query(
                new QuerySalaryMerchantFlow()
                        .setTenantIds(tenantIds)
                        .setFlowNos(rechargeFlowMap.keySet()));

        if (CollUtil.isNotEmpty(existFlowEntityList)) {
            MapUtil.removeAny(rechargeFlowMap, existFlowEntityList.stream().map(SalaryMerchantFlowEntity::getFlowNo).toArray(String[]::new));
        }
        Collection<AssetXhServerProto.CmbBillFlowResponse> insetList = rechargeFlowMap.values();
        if (CollUtil.isNotEmpty(insetList)) {
            List<SalaryMerchantFlowEntity> flowEntityList = Lists.newArrayList();
            for (AssetXhServerProto.CmbBillFlowResponse flowResponse : insetList) {
                SalaryMerchantInfoEntity salaryMerchantInfo = subAccountMapperTenantId.get(flowResponse.getSubAccountId());
                if (salaryMerchantInfo == null) {
                    continue;
                }
                flowEntityList.add(SalaryConvert.response2Entity(flowResponse, salaryMerchantInfo.getId(), salaryMerchantInfo.getPayeeMerchantName(), salaryMerchantInfo.getPayeeSubAccountId()));
            }
            salaryMerchantFlowRepository.batchInsert(flowEntityList);
        }
    }
}
