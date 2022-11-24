package cn.xunhou.web.xbbcloud.product.hrm.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ImportStaffEmployeeCheckQueryParam extends PageInfo {

    private String key;
}
