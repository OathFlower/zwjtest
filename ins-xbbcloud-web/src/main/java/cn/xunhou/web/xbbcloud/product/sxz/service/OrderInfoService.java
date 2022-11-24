package cn.xunhou.web.xbbcloud.product.sxz.service;


import cn.hutool.core.collection.CollectionUtil;
import cn.xunhou.cloud.core.util.SystemUtil;
import cn.xunhou.web.xbbcloud.product.sxz.dao.OrderRepository;
import cn.xunhou.web.xbbcloud.product.sxz.entity.OrderEntity;
import cn.xunhou.web.xbbcloud.product.sxz.enums.RmbPackageEnum;
import cn.xunhou.web.xbbcloud.product.sxz.enums.WxTradeStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class OrderInfoService {

    @Autowired
    private OrderRepository orderRepository;

    public OrderEntity createOrderByProductId(Long userId, Long productId) {
        //查找已存在但未支付的订单
      /*  OrderEntity orderInfo = this.getNoPayOrderByProductId(productId, userId);
        if (orderInfo != null) {
            return orderInfo;
        }*/

        //获取商品信息
        RmbPackageEnum rmbPackageEnum = RmbPackageEnum.getEnum(productId.intValue());

        //生成订单
        OrderEntity orderInfo = new OrderEntity();
        orderInfo.setTitle(rmbPackageEnum.getMsg());
        orderInfo.setProductId(productId);
        orderInfo.setUserId(userId);
        orderInfo.setPayableFee(rmbPackageEnum.getOriginCoin() * 100);//分
        if (SystemUtil.isOffline()) {
            orderInfo.setPaymentFee(1); //分
        } else {
            if (userId == 45 || userId == 35 || userId == 29 || userId == 51) { //线上测试人员的userId
                orderInfo.setPaymentFee(1); //分
            } else {
                orderInfo.setPaymentFee(rmbPackageEnum.getNowCoin() * 100); //分
            }

        }
        orderInfo.setWxStatus(WxTradeStateEnum.NOTPAY.getType());
        Long saveId = orderRepository.insert(orderInfo).longValue();
        orderInfo.setId(saveId);
        return orderInfo;
    }


    /**
     * 存储订单预支付号
     *
     * @param orderId
     * @param prepayId
     */

    public void savePrepayId(Long orderId, String prepayId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPrepayId(prepayId);
        orderRepository.updateById(orderId, orderEntity);
    }


    /**
     * 根据订单号更新订单状态
     *
     * @param orderId
     * @param orderStatus
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusByOrderId(Long orderId, WxTradeStateEnum orderStatus) {

        log.info("更新订单状态 ===> {}", orderStatus.getType());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setWxStatus(orderStatus.getType());
        orderRepository.updateById(orderId, orderEntity);
    }

    /**
     * 根据订单号获取订单状态
     *
     * @param orderId
     * @return
     */

    public String getOrderStatus(Long orderId) {

        OrderEntity orderEntity = orderRepository.queryOrderById(orderId);
        if (orderEntity != null) {
            return orderEntity.getWxStatus();
        }
        return null;
    }

    /**
     * 查询创建超过minutes分钟并且未支付的订单
     *
     * @param minutes
     * @return
     */

    public List<OrderEntity> getNoPayOrderByDuration(int minutes) {
        return orderRepository.getNoPayOrderByDuration(minutes);

    }



    /**
     * 根据商品id查询未支付订单
     * 防止重复创建订单对象
     *
     * @param productId
     * @return
     */
    private OrderEntity getNoPayOrderByProductId(Long productId, Long userId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setProductId(productId);
        orderEntity.setUserId(userId);
        orderEntity.setWxStatus(WxTradeStateEnum.NOTPAY.getType());
        List<OrderEntity> orderEntityList = orderRepository.queryOrderList(orderEntity);
        if (CollectionUtil.isNotEmpty(orderEntityList)) {
            return orderEntityList.get(0);
        }
        return null;
    }
}
