package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class StaffAddRepResult {
    private String index;
    /** 证件号是否存在 */
    private Boolean repeatIdcardNoFlag = false;
}
