package cn.xunhou.web.xbbcloud.product.hrm.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class StaffAddRepQueryParam {
    private String index;
    /** 证件号 */
    private String idcardNo;

    public StaffAddRepQueryParam(String index, String idcardNo) {
        this.index = index;
        this.idcardNo = idcardNo;
    }

    public StaffAddRepQueryParam() {
    }
}
