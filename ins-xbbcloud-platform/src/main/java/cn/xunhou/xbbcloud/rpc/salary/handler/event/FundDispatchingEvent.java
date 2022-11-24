package cn.xunhou.xbbcloud.rpc.salary.handler.event;

import cn.xunhou.xbbcloud.rpc.salary.entity.FundDispatchingEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 资金调度事件
 *
 * @author wangkm
 */
@Getter
@Setter
public class FundDispatchingEvent extends ApplicationEvent {

    /**
     * 交易主键-串起交易 （可使用batchId）
     */
    private String transactionMain;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 调度成功 返回true
     */
    private Boolean dispatchSuccess;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 调度详情
     */
    private List<FundDispatchingEntity> fundDispatchingEntityList;

    /**
     * 调度完成
     */
    private Boolean fundDispatchingEnd;

    public FundDispatchingEvent(Object source, Long tenantId, Boolean dispatchSuccess, String transactionMain, String failureReason, List<FundDispatchingEntity> fundDispatchingEntityList
            , Boolean fundDispatchingEnd
    ) {
        super(source);
        this.tenantId = tenantId;
        this.dispatchSuccess = dispatchSuccess;
        this.transactionMain = transactionMain;
        this.failureReason = failureReason;
        this.fundDispatchingEntityList = fundDispatchingEntityList;
        this.fundDispatchingEnd = fundDispatchingEnd;
    }
}
