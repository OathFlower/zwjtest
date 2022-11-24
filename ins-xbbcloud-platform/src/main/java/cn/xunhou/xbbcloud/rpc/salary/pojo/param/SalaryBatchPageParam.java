package cn.xunhou.xbbcloud.rpc.salary.pojo.param;

import cn.xunhou.xbbcloud.rpc.other.pojo.param.PageBaseParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryBatchPageParam extends PageBaseParam {
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 发薪日期 开始 对应创建时间
     */
    private Timestamp startSubmitTime;

    /**
     * 发薪日期 截至 对应创建时间
     */
    private Timestamp endSubmitTime;


    /**
     * 状态  0进行中 1全部成功 2部分成功 3全部失败
     */
    private Integer status;

    /**
     * 批次编号
     */
    private Long batchId;


    /**
     * 租户id
     */
    private Long tenantId;

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


    private List<Long> batchIdList;
    /**
     * 扣费状态 0成功 1失败
     */
    private List<Integer> deductionStatusList;


    /**
     * 是否是运营平台 true是 false否
     */
    private boolean isOperation;


}
