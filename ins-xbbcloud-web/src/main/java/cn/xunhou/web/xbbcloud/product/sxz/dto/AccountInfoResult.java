package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 用户信息
 */
@ToString
@Data
@Accessors(chain = true)
public class AccountInfoResult {


    /**
     * 手机号
     */
    private String tel;


    /**
     * 余额
     */
    private Integer coin = 0;


}
