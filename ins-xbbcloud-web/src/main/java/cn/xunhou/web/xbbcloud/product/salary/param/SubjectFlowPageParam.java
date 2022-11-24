package cn.xunhou.web.xbbcloud.product.salary.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubjectFlowPageParam extends PageInfo {
    /**
     * 主体ids
     */
    private List<Long> subjectIds;

    /**
     * 租户ids
     */
    private List<Long> tenantIds;

    /**
     * 子账户ids
     */
    private List<Long> subAccountIds;
}
