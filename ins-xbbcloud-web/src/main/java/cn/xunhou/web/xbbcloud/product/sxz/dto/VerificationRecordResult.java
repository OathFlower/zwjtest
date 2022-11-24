package cn.xunhou.web.xbbcloud.product.sxz.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 核销记录
 */
@ToString
@Data
@Accessors(chain = true)
public class VerificationRecordResult {


    /**
     * 虚拟币
     */
    private Integer coin;
    /**
     * 消费产品名称
     */
    private String title;
    /**
     * 使用企业
     */
    private String customerName;
    /**
     * 购买说明
     */
    private String remark;
    /**
     * 创建时间
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updatedAt;
}
