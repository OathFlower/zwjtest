package cn.xunhou.web.xbbcloud.product.hrm.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author litb
 * @date 2022/9/19 17:53
 * <p>
 * 字典保存参数
 */
@Getter
@Setter
@ToString
public class EmployeeDictionarySaveParam {

    /**
     * 字典id 有为编辑 无则为新增
     */
    private Long id;

    /**
     * 字典名称,必传
     */
    @NotBlank(message = "名称不能为空")
    @Length(max = 255, message = "最长为255个字符")
    private String name;

    /**
     * 字典类型,1用工类型 2用工来源,必传
     */
    @NotNull(message = "字典类型不能为空")
    private Integer type;

    /**
     * 描述
     */
    private String description;
}
