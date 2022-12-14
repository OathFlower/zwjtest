package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.cloud.core.context.UserParam;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbCamelJsonUtil;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.web.xbbcloud.product.sxz.dao.UserRepository;
import cn.xunhou.web.xbbcloud.product.sxz.dao.VirtualFlowRepository;
import cn.xunhou.web.xbbcloud.product.sxz.entity.ServiceOrderEntity;
import cn.xunhou.web.xbbcloud.product.sxz.entity.UserEntity;
import cn.xunhou.web.xbbcloud.product.sxz.entity.VirtualFlowEntity;
import cn.xunhou.web.xbbcloud.product.sxz.param.ConsumeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.RechargeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.VerificationParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.VerificationRecordParam;
import cn.xunhou.web.xbbcloud.product.sxz.dao.ServiceOrderRepository;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ConsumeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.HasServiceRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RechargeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.VerificationRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.enums.ServiceOrderStatusEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.ServicePackageEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.ServiceTypeEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.VirtualFlowStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ServiceOrderService {

    @Resource
    private ServiceOrderRepository serviceOrderRepository;
    @Resource
    private UserRepository userRepository;
    @Resource
    private VirtualFlowRepository virtualFlowRepository;
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    /**
     * ??????????????????
     *
     * @param productId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse saveServiceOrder(Long productId) {
        UserParam userParam = XBB_USER_CONTEXT.get();
        ServicePackageEnum servicePackageEnum = ServicePackageEnum.getEnum(productId.intValue());
        ServiceOrderEntity serviceOrderEntity = new ServiceOrderEntity();
        serviceOrderEntity.setCoin(servicePackageEnum.getNowCoin());
        serviceOrderEntity.setTitle(servicePackageEnum.getMsg());
        serviceOrderEntity.setProductId(productId);
        serviceOrderEntity.setServiceType(ServiceTypeEnum.BUY.getCode());
        serviceOrderEntity.setStatus(ServiceOrderStatusEnum.UN_USE.getCode());
        UserEntity userEntity = userRepository.getUserById(userParam.getUserId());
        //??????????????????????????????
        if (userEntity.getCoin() < servicePackageEnum.getNowCoin()) {
            return JsonResponse.systemError("???????????????");
        }
        serviceOrderEntity.setUserId(userEntity.getId());
        serviceOrderEntity.setCustomerId(userEntity.getCustomerId());
        //?????????????????????
        Long saveServiceOrderId = serviceOrderRepository.insert(serviceOrderEntity).longValue();
        //??????????????????
        userEntity.setCoin(userEntity.getCoin() - servicePackageEnum.getNowCoin());
        userRepository.updateById(userEntity.getId(), userEntity);
        //??????????????????
        VirtualFlowEntity virtualFlowEntity = new VirtualFlowEntity();
        virtualFlowEntity.setUserId(userEntity.getId());
        virtualFlowEntity.setCustomerId(userEntity.getCustomerId());
        virtualFlowEntity.setFlowType(VirtualFlowStatusEnum.CONSUME.getCode());//??????
        virtualFlowEntity.setObjectId(saveServiceOrderId);
        virtualFlowEntity.setCoin(servicePackageEnum.getNowCoin());
        virtualFlowRepository.insert(virtualFlowEntity);

        return JsonResponse.success();
    }

    /**
     * ???????????????
     *
     * @return
     */
    public JsonResponse<List<HasServiceRecordResult>> hasServiceRecord(Long userId) {
        List<HasServiceRecordResult> hasServiceRecordResultList = new ArrayList<>();
        ServiceOrderEntity queryParam = new ServiceOrderEntity();
        if (userId != null) {
            queryParam.setUserId(userId);
        } else {
            queryParam.setUserId(XBB_USER_CONTEXT.get().getUserId());
        }
        queryParam.setStatus(ServiceOrderStatusEnum.UN_USE.getCode());
        List<ServiceOrderEntity> serviceOrderEntityList = serviceOrderRepository.queryServiceOrderList(queryParam);
        if (CollectionUtil.isNotEmpty(serviceOrderEntityList)) {
            serviceOrderEntityList.stream().collect(Collectors.groupingBy(ServiceOrderEntity::getProductId, Collectors.counting())).forEach((key, value) -> {
                HasServiceRecordResult hasServiceRecordResult = new HasServiceRecordResult();
                hasServiceRecordResult.setCount(value);
                hasServiceRecordResult.setProductId(key);
                hasServiceRecordResult.setTitle(ServicePackageEnum.getEnum(key.intValue()).getMsg());
                hasServiceRecordResultList.add(hasServiceRecordResult);
            });
        }
        return JsonResponse.success(hasServiceRecordResultList);
    }

    /**
     * ????????????
     *
     * @param param
     * @return
     */
    public JsonListResponse<RechargeRecordResult> rechargeRecord(RechargeRecordPageParam param) {

        log.info("???????????? ??????:" + XbbCamelJsonUtil.toJsonString(param));
        PagePojoList<RechargeRecordResult> rechargeRecordResultPagePojoList = virtualFlowRepository.rechargePageList(param);
        if (Objects.isNull(rechargeRecordResultPagePojoList) || CollUtil.isEmpty(rechargeRecordResultPagePojoList.getData())) {
            return JsonListResponse.success();
        }
        return JsonListResponse.success(rechargeRecordResultPagePojoList.getData(), rechargeRecordResultPagePojoList.getTotal());
    }

    /**
     * ????????????
     *
     * @param param
     * @return
     */
    public JsonListResponse<ConsumeRecordResult> consumeRecord(ConsumeRecordPageParam param) {

        log.info("???????????? ??????:" + XbbCamelJsonUtil.toJsonString(param));
        PagePojoList<ConsumeRecordResult> consumeRecordResultPagePojoList = virtualFlowRepository.consumePageList(param);
        if (Objects.isNull(consumeRecordResultPagePojoList) || CollUtil.isEmpty(consumeRecordResultPagePojoList.getData())) {
            return JsonListResponse.success();
        }
        return JsonListResponse.success(consumeRecordResultPagePojoList.getData(), consumeRecordResultPagePojoList.getTotal());
    }

    /**
     * ??????
     *
     * @param param
     * @return
     */
    public JsonResponse writeOff(VerificationParam param) {
        //?????????userId?????????id????????????????????? ????????????list
        ServiceOrderEntity serviceOrderEntityQurery = new ServiceOrderEntity();
        serviceOrderEntityQurery.setStatus(ServiceOrderStatusEnum.UN_USE.getCode());
        serviceOrderEntityQurery.setUserId(param.getUserId());
        serviceOrderEntityQurery.setProductId(param.getProductId());
        List<ServiceOrderEntity> serviceOrderEntityList = serviceOrderRepository.queryServiceOrderList(serviceOrderEntityQurery);
        if (CollectionUtil.isNotEmpty(serviceOrderEntityList)) {
            //get(0) ??????update??????????????????
            ServiceOrderEntity serviceOrderEntity = serviceOrderEntityList.get(0);
            serviceOrderEntity.setStatus(ServiceOrderStatusEnum.USED.getCode());
            //???????????????
            serviceOrderEntity.setOperatorId(XBB_USER_CONTEXT.get().getUserId());
            serviceOrderEntity.setUpdatedAt(new Date().getTime());
            serviceOrderEntity.setCustomerName(param.getCustomerName());
            serviceOrderEntity.setRemark(param.getRemark());
            serviceOrderRepository.updateById(serviceOrderEntity.getId(), serviceOrderEntity);
        } else {
            return JsonResponse.systemError("???????????????");
        }
        return JsonResponse.success();
    }

    public JsonListResponse<VerificationRecordResult> writeOffRecord(VerificationRecordParam param) {
        PagePojoList<VerificationRecordResult> verificationRecordResultPagePojoList = serviceOrderRepository.verificationRecord(param);
        if (Objects.isNull(verificationRecordResultPagePojoList) || CollUtil.isEmpty(verificationRecordResultPagePojoList.getData())) {
            return JsonListResponse.success();
        }
        return JsonListResponse.success(verificationRecordResultPagePojoList.getData(), verificationRecordResultPagePojoList.getTotal());
    }
}
