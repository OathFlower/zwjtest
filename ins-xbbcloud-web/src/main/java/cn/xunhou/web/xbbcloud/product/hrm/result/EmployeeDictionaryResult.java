package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author litb
 * @date 2022/9/19 17:51
 * <p>
 * 员工字典返回结果
 */
@Getter
@Setter
@ToString
public class EmployeeDictionaryResult {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 字典code
     */
    private Integer code;

    /**
     * 字典名称,例如 全职 兼职 勋厚
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否是可以编辑的,true可以编辑/删除 false不可编辑
     */
    private Boolean editable;

}
