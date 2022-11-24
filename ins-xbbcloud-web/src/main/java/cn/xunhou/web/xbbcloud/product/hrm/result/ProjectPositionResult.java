package cn.xunhou.web.xbbcloud.product.hrm.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ProjectPositionResult {
    /**
     * 岗位ID
     */
    private Long id;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 工作地点
     */
    private String areaCode;

    /**
     * 职能
     */
    private String jobCode;

}
