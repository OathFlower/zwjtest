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
public class SalaryDetailPageParam extends PageBaseParam {
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 员工姓名
     */
    private String staffName;

    /**
     * 发薪日期 开始 对应创建时间
     */
    private Timestamp startSubmitTime;

    /**
     * 发薪日期 截至 对应创建时间
     */
    private Timestamp endSubmitTime;

    /**
     * 查询更新时间开始
     */
    private Timestamp updateTimeStart;

    /**
     * 查询更新时间截至
     */
    private Timestamp updateTimeEnd;

    /**
     * 身份证号
     */
    private String idCardNo;
    /**
     * 手机号
     */
    private String phone;

    /**
     * 明细状态  状态 0支付处理中（未认证） 1支付处理中（已下单） 1已发薪 2支付失败
     */
    private List<Integer> detailStatus;
    /**
     * notIn状态的list
     */
    private List<Integer> notInDetailStatus;


    /**
     * ids
     */
    private List<Long> ids;

    /**
     * 身份证号
     */
    private List<String> idCardNoList;
    /**
     * id
     */
    private Long id;

    /**
     * 批次编号
     */
    private Long batchId;


    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 租户id
     */
    private List<Long> tenantIdList;
    /**
     * 批次号list
     */
    private List<Long> batchIdList;


    /**
     * 是否是运营平台 true是 false否
     */
    private boolean isOperation;

}
