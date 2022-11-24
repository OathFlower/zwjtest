package cn.xunhou.web.xbbcloud.product.salary.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SubAccountFlowPageParam extends PageInfo {
    /**
     * 子账户ids
     */
    @NotEmpty(message = "子账户id不能为空")
    private List<Long> subAccountIds;

    /**
     * 操作类型： 0支出 1收入
     */
    private List<Integer> operationType;

    /**
     * 创建时间 开始时间 yyyy-MM-dd hh:mm:ss
     */
    private String startTime;
    /**
     * 创建时间 结束时间 yyyy-MM-dd hh:mm:ss
     */
    private String endTime;

    /**
     * //锁定
     * FT_LOCKED = 0;
     * //取消
     * FT_CANCELED = 1;
     * //完成
     * FT_COMPLETED = 2;
     * //处理中
     * FT_IN_PROGRESS = 3;
     * //等待处理
     * FT_WAITING = 4;
     */
    private List<Integer> flowStates;
}
