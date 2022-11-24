package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.DesensitizedUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.json.XbbJsonUtil;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.web.xbbcloud.product.sxz.dao.*;
import cn.xunhou.web.xbbcloud.product.sxz.dto.AccountInfoResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.AccountMultipleResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.HasServiceRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.WeChatNotify;
import cn.xunhou.web.xbbcloud.product.sxz.entity.*;
import cn.xunhou.web.xbbcloud.product.sxz.enums.*;
import cn.xunhou.web.xbbcloud.product.sxz.param.AccountMultiplePageParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserService {

    @Resource
    private UserRepository userRepository;
    @Resource
    private CustomerRepository customerRepository;
    @Resource
    private OrderRepository orderRepository;
    @Resource
    private ServiceOrderRepository serviceOrderRepository;
    @Resource
    private VirtualFlowRepository virtualFlowRepository;
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    public UserEntity getUserByMobile(String tel) {
        return userRepository.getUserByMobile(tel);
    }


    @Transactional(rollbackFor = Exception.class)
    public Long register(UserEntity userEntity) {

        Long saveUserId = userRepository.insert(userEntity).longValue();
        CustomerEntity customerEntity = new CustomerEntity();
        Long customerSaveId = customerRepository.insert(customerEntity).longValue();
        UserEntity updateUserEntity = new UserEntity();
        updateUserEntity.setCustomerId(customerSaveId);
        userRepository.updateById(saveUserId, updateUserEntity);
        return saveUserId;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCoin(String plainText) {
    /*    Gson gson = new Gson();
        HashMap plainTextMap = gson.fromJson(plainText, HashMap.class);*/
        WeChatNotify weChatNotify = XbbJsonUtil.fromJsonString(plainText, WeChatNotify.class);
        //订单号
        Long orderId = Long.valueOf(weChatNotify.getOut_trade_no());

        OrderEntity orderEntity = orderRepository.queryOrderById(orderId);
        Long userId = orderEntity.getUserId();

        Integer addCoin = RmbPackageEnum.getEnum(orderEntity.getProductId().intValue()).getOriginCoin();
        UserEntity userOldEntity = userRepository.getUserById(userId);
        UserEntity updateUserEntity = new UserEntity();
        updateUserEntity.setCoin(userOldEntity.getCoin() + addCoin);
        userRepository.updateById(userId, updateUserEntity);
        //插入充值流水
        VirtualFlowEntity virtualFlowEntity = new VirtualFlowEntity();
        virtualFlowEntity.setUserId(userId);
        virtualFlowEntity.setCustomerId(userOldEntity.getCustomerId());
        virtualFlowEntity.setFlowType(VirtualFlowStatusEnum.RECHARGE.getCode());//充值
        virtualFlowEntity.setObjectId(orderId);
        virtualFlowEntity.setCoin(addCoin);
        virtualFlowRepository.insert(virtualFlowEntity);


        //查询是否有已赠送服务
        ServiceOrderEntity serviceOrderQuery = new ServiceOrderEntity();
        serviceOrderQuery.setUserId(userOldEntity.getId());
        serviceOrderQuery.setServiceType(ServiceTypeEnum.PRESENT.getCode());
        List<ServiceOrderEntity> serviceOrderEntityList = serviceOrderRepository.queryServiceOrderList(serviceOrderQuery);
        if (CollectionUtil.isEmpty(serviceOrderEntityList)) {
            List<ServiceOrderEntity> presentEntityList = new ArrayList<>();
            //赠送线下双选会1次，线上双选会3次
            ServiceOrderEntity offlineTwoWayEntity = new ServiceOrderEntity();
            offlineTwoWayEntity.setCoin(ServicePackageEnum.OFFLINE_TWO_WAY.getNowCoin());
            offlineTwoWayEntity.setTitle(ServicePackageEnum.OFFLINE_TWO_WAY.getMsg());
            offlineTwoWayEntity.setProductId(ServicePackageEnum.OFFLINE_TWO_WAY.getCode().longValue());
            offlineTwoWayEntity.setServiceType(ServiceTypeEnum.PRESENT.getCode());
            offlineTwoWayEntity.setStatus(ServiceOrderStatusEnum.UN_USE.getCode());
            offlineTwoWayEntity.setUserId(userOldEntity.getId());
            offlineTwoWayEntity.setCustomerId(userOldEntity.getCustomerId());
            serviceOrderRepository.insert(offlineTwoWayEntity);
            for (int i = 1; i <= 3; i++) {
                ServiceOrderEntity onlineRecruitmentEntity = new ServiceOrderEntity();
                onlineRecruitmentEntity.setCoin(ServicePackageEnum.ONLINE_RECRUITMENT.getNowCoin());
                onlineRecruitmentEntity.setTitle(ServicePackageEnum.ONLINE_RECRUITMENT.getMsg());
                onlineRecruitmentEntity.setProductId(ServicePackageEnum.ONLINE_RECRUITMENT.getCode().longValue());
                onlineRecruitmentEntity.setServiceType(ServiceTypeEnum.PRESENT.getCode());
                onlineRecruitmentEntity.setStatus(ServiceOrderStatusEnum.UN_USE.getCode());
                onlineRecruitmentEntity.setUserId(userOldEntity.getId());
                onlineRecruitmentEntity.setCustomerId(userOldEntity.getCustomerId());
                serviceOrderRepository.insert(onlineRecruitmentEntity);
            }

        }
    }

    /**
     * 用户综合信息列表
     *
     * @return
     */
    public JsonListResponse<AccountMultipleResult> list(AccountMultiplePageParam param) {

        PagePojoList<AccountMultipleResult> accountMultipleResultPagePojoList = userRepository.accountMultipleList(param);
        if (Objects.isNull(accountMultipleResultPagePojoList) || CollUtil.isEmpty(accountMultipleResultPagePojoList.getData())) {
            return JsonListResponse.success();
        }
        //查询享有的服务权益
        for (AccountMultipleResult accountMultipleResult :
                accountMultipleResultPagePojoList.getData()) {
            accountMultipleResult.setTel(DesPlus.getInstance().decrypt(accountMultipleResult.getTel()));
            List<HasServiceRecordResult> hasServiceRecordResultList = new ArrayList<>();
            ServiceOrderEntity queryParam = new ServiceOrderEntity();
            queryParam.setUserId(accountMultipleResult.getId());
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
            accountMultipleResult.setHasServiceRecordResultList(hasServiceRecordResultList);
        }
        return JsonListResponse.success(accountMultipleResultPagePojoList.getData(), accountMultipleResultPagePojoList.getTotal());
    }

    public JsonResponse<AccountInfoResult> info() {
        AccountInfoResult accountInfoResult = new AccountInfoResult();
        log.info("用户为" + XBB_USER_CONTEXT.get().getUserId());
        UserEntity userEntity = userRepository.getUserById(XBB_USER_CONTEXT.get().getUserId());
        accountInfoResult.setCoin(userEntity.getCoin() == null ? 0 : userEntity.getCoin());
        accountInfoResult.setTel(DesensitizedUtil.mobilePhone(DesPlus.getInstance().decrypt(userEntity.getTel())));
        return JsonResponse.success(accountInfoResult);
    }
}
