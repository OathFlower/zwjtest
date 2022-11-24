package cn.xunhou.web.xbbcloud.product.sxz.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.cloud.dao.pojo.PagePojoList;
import cn.xunhou.cloud.framework.util.DesPlus;
import cn.xunhou.web.xbbcloud.product.sxz.dao.*;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ReceiptRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.RechargeRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.entity.CustomerEntity;
import cn.xunhou.web.xbbcloud.product.sxz.entity.ReceiptEntity;
import cn.xunhou.web.xbbcloud.product.sxz.entity.UserEntity;
import cn.xunhou.web.xbbcloud.product.sxz.param.RechargeRecordPageParam;
import cn.xunhou.web.xbbcloud.product.sxz.param.SaveReceiptParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 发票service
 */
@Service
@Slf4j
public class ReceiptService {

    @Resource
    private ReceiptRepository receiptRepository;
    @Resource
    private VirtualFlowRepository virtualFlowRepository;
    @Resource
    private UserRepository userRepository;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private CustomerRepository customerRepository;
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();

    /**
     * 保存发票
     *
     * @param saveReceiptParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonResponse saveReceipt(SaveReceiptParam saveReceiptParam) throws Exception {
        //先查询要开发票的订单 是否已经有开过票的
        RechargeRecordPageParam param = new RechargeRecordPageParam();
        param.setOrderIds(saveReceiptParam.getOrderIds());
        PagePojoList<RechargeRecordResult> rechargeRecordResultPagePojoList = virtualFlowRepository.rechargePageList(param);
        if (!Objects.isNull(rechargeRecordResultPagePojoList) && CollUtil.isNotEmpty(rechargeRecordResultPagePojoList.getData())) {
            return JsonResponse.systemError("请勿重复申请开票");
        }

        //保存一条记录到发票表
        ReceiptEntity receiptEntity = new ReceiptEntity();
        BeanUtils.copyProperties(saveReceiptParam, receiptEntity);
        UserEntity userEntity = userRepository.getUserById(XBB_USER_CONTEXT.get().getUserId());

        receiptEntity.setUserId(userEntity.getId());
        receiptEntity.setCustomerId(userEntity.getCustomerId());
        Long saveReceiptId = receiptRepository.insert(receiptEntity).longValue();
        //更新订单表的发票id
        orderRepository.batchUpdateReceipt(saveReceiptParam.getOrderIds(), saveReceiptId);

        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setCustomerName(saveReceiptParam.getCustomerName());
        customerEntity.setAddress(saveReceiptParam.getAddress());
        customerEntity.setTaxNo(saveReceiptParam.getTaxNo());
        customerEntity.setId(userEntity.getCustomerId());
        List<CustomerEntity> customerEntityList = customerRepository.queryRepList(customerEntity);
        if (CollectionUtil.isNotEmpty(customerEntityList)) {
            throw new Exception("已有重复的公司或税号，请重新填写");
        }
        customerRepository.updateById(userEntity.getCustomerId(), customerEntity);
        return JsonResponse.success();
    }

    public JsonListResponse<ReceiptRecordResult> record(PageInfo param) {
        PagePojoList<ReceiptRecordResult> receiptRecordResultPagePojoList = receiptRepository.record(param);
        if (Objects.isNull(receiptRecordResultPagePojoList) || CollUtil.isEmpty(receiptRecordResultPagePojoList.getData())) {
            return JsonListResponse.success();
        }
        for (ReceiptRecordResult receiptRecordResult :
                receiptRecordResultPagePojoList.getData()) {
            receiptRecordResult.setTel(DesPlus.getInstance().decrypt(receiptRecordResult.getTel()));
        }
        return JsonListResponse.success(receiptRecordResultPagePojoList.getData(), receiptRecordResultPagePojoList.getTotal());
    }
}
