package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryDetailQueryListParam {


    /**
     * 批次编号
     */
    private Long batchId;


    /**
     * 租户id
     */
    private Long tenantId;

}
