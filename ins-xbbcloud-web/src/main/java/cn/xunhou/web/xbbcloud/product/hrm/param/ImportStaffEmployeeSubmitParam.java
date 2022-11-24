package cn.xunhou.web.xbbcloud.product.hrm.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ImportStaffEmployeeSubmitParam {

    private String key;
    private List<Long> indexList;
}
