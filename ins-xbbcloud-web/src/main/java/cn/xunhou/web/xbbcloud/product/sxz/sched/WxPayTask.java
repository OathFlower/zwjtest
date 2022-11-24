/*

package cn.xunhou.web.xbbcloud.sched;

import cn.xunhou.web.xbbcloud.entity.OrderEntity;
import cn.xunhou.web.xbbcloud.service.OrderInfoService;
import cn.xunhou.web.xbbcloud.service.WxPayService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class WxPayTask {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private WxPayService wxPayService;


    */
/**
 * 从第0秒开始每隔30秒执行1次，查询创建超过5分钟，并且未支付的订单
 *//*


    //@Scheduled(cron = "0/30 * * * * ?")
    @XxlJob("orderConfirm")
    public void orderConfirm() throws Exception {
        log.info("orderConfirm 被执行......");

        List<OrderEntity> orderInfoList = orderInfoService.getNoPayOrderByDuration(1);

        for (OrderEntity orderInfo : orderInfoList) {
            Long orderId = orderInfo.getId();
            log.warn("超时订单 ===> {}", orderId);

            //核实订单状态：调用微信支付查单接口
            wxPayService.checkOrderStatus(orderId);
        }
    }


}

*/
