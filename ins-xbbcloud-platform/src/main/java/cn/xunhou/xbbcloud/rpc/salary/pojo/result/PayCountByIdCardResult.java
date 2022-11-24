package cn.xunhou.xbbcloud.rpc.salary.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PayCountByIdCardResult implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 身份证号 加解密
     */
    private String idCardNo;


    /**
     * 应发金额（分） 总数
     */
    private Integer totalPaidAble;

    /**
     * 个税（分）总数
     */
    private Integer totalTax;


}
