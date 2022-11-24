package cn.xunhou.web.xbbcloud.product.salary.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryProductResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目名
     */
    private String name;

}
