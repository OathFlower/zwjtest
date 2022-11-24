package cn.xunhou.web.xbbcloud.product.salary.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryBatchPageParam extends PageInfo {
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 发薪日期 开始 对应创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String startSubmitTime;

    /**
     * 发薪日期 截至 对应创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String endSubmitTime;


    /**
     * 状态  0进行中 1全部成功 2部分成功 3全部失败
     */
    private Integer status;

    /**
     * 批次编号
     */
    private Long batchId;
    /**
     * 发薪方式 1小程序提现  2微信转账
     */
    private Integer payMethod;

    /**
     * 主体id
     */
    private Long subjectId;

    /**
     * 明细状态list
     */
    private List<Integer> statusList;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 扣费状态
     */
    private List<Integer> deductionStatusList;


}
