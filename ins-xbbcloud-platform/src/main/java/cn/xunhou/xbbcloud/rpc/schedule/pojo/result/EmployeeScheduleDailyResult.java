package cn.xunhou.xbbcloud.rpc.schedule.pojo.result;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author litb
 * @date 2022/9/15 14:05
 * <p>
 * 按天展示指定员工的排班结果
 */
@Getter
@Setter
@ToString
@Builder
public class EmployeeScheduleDailyResult {

    private Long employeeId;

    private Long orgId;
}
