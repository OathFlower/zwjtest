package cn.xunhou.xbbcloud.rpc.approve.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * @Author: chenning
 * @Date: 2022/09/28/20:27
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class WorkflowFormQueryParam extends PageInfo {


    private Long insId;
    //申请开始时间
    private Timestamp startTime;
    //申请结束时间
    private Timestamp endTime;
    //申请人ID
    private List<Long> applicantIds;
    //需要排除的申请人ID
    private List<Long> excludeApplicantIds;
    //审核状态  0未发起 1审批中 2审批通过 3审批驳回 4撤销
    private Integer status;
    //审批时间
    private Timestamp approvalTime;
    //模板类型id
    private List<Long> flowTemplateId;
    /**
     * 数据Json
     */
    private String newJson;
    /**
     * 租户ID
     */
    private List<Integer> tenantIds;
    /**
     * 目标编辑ID
     */
    private List<Long> editTargetIds;
    /**
     * 虚拟字段
     */
    private String v0;

    /**
     * 虚拟字段
     */
    private String v1;

    /**
     * 虚拟字段
     */
    private String v2;

    /**
     * 虚拟字段
     */
    private String v3;

    /**
     * 虚拟字段
     */
    private String v4;

    /**
     * 虚拟字段
     */
    private String v5;

    /**
     * 虚拟字段
     */
    private String v6;

    /**
     * 虚拟字段
     */
    private String v7;

    /**
     * 虚拟字段
     */
    private String v8;

    /**
     * 虚拟字段
     */
    private String v9;

    /**
     * 虚拟扩展字段
     */
    private String ext;

    private List<Long> filterInsIds;

    /**
     * 排序字段
     */
    private String sortField;

    private Timestamp flowStartTime;

    private List<Integer> statuses;

    private String jsonKey;

    private Object jsonValue;

    private List<String> values;

}
