package cn.xunhou.web.xbbcloud.product.sxz.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 充值记录
 */
@ToString
@Data
@Accessors(chain = true)
public class ConsumeRecordResult {


    /**
     * 虚拟币
     */
    private Integer coin;
    /**
     * 消费产品名称
     */
    private String title;


    /**
     * 创建时间
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdAt;
}
