package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "salary_product")
public class SalaryProductEntity extends XbbSnowTimeEntity {


    /**
     * 项目名称
     */
    private String name;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 操作人id
     */
    private Long operatorId;

}
