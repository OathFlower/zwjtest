package cn.xunhou.web.xbbcloud.product.manage.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 商务合同项目信息
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class ContractProjectResult {
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 项目名
     */
    private String projectName;

}
