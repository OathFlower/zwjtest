package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ImportSalaryResult<T> extends ImportResult<T> {
    /**
     * 数字金额总数
     */
    private String numCount;
    /**
     * 中文金额总数
     */
    private String chinaCount;
}
