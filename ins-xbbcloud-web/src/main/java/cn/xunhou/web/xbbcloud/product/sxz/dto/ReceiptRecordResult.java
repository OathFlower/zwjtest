package cn.xunhou.web.xbbcloud.product.sxz.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 开票记录
 */
@ToString
@Data
@Accessors(chain = true)
public class ReceiptRecordResult {

    /**
     * 发票id
     */
    private Long id;
    /**
     * 手机号
     */
    private String tel;
    /**
     * 开票金额（单位 分）
     */
    private Integer totalFee;

    /**
     * 公司名称
     */
    private String customerName;
    /**
     * 公司税号
     */
    private String taxNo;
    /**
     * 地址
     */
    private String address;
    /**
     * 申请时间
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createdAt;
}
