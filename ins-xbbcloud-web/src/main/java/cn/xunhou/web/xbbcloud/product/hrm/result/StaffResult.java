package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 查询人力员工信息
 *
 * @author TangYitong
 * @date 2022/10/25
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class StaffResult {
    /**
     * 员工id
     */
    private Long id;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 联系方式
     */
    private String telephone;

    /**
     * 身份证号
     */
    private String idcardNo;

    /**
     * 证件类型
     */
    private Integer certificateCode;

    /**
     * 证件号码
     */
    private String certificateNo;

    /**
     * 入职日期
     */
    private String onboardDate;

}
