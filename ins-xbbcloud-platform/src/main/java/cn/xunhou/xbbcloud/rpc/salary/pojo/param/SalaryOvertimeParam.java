package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryOvertimeParam {
    //批次id
    private Long batchId;
    //偏移日期 偏移天数，正数向未来偏移，负数向历史偏移
    private Integer dateOffset;
}
