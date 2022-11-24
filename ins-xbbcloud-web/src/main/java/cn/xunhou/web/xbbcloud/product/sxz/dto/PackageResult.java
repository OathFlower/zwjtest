package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


@ToString
@Data
@Accessors(chain = true)
public class PackageResult {
    private Integer code;

    private String msg;
    /**
     * 原价
     */
    private Integer originCoin;
    /**
     * 现价
     */
    private Integer nowCoin;
}
