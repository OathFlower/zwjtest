package cn.xunhou.web.xbbcloud.product.sxz.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分页查询消费记录
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class RechargeRecordPageParam extends PageInfo {
       /**
        * 查询未开票记录
        */
       private Boolean unReceiptFlag = false;
       /**
        * 订单ids
        */
       private List<Long> orderIds;
}
