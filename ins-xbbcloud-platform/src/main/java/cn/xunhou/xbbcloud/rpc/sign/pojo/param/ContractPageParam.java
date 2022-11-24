package cn.xunhou.xbbcloud.rpc.sign.pojo.param;

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
public class ContractPageParam extends PageBaseParam {

    private Long id;
    private List<Long> ids;
    /**
     * 排除的id
     */
    private List<Long> excludeIds;
    /**
     * 身份证号
     */
    private String idCardNo;
    private List<String> idCardNos;

    /**
     * 员工id
     */
    private Long employeeId;
    private List<Long> employeeIds;

    /**
     * 数据来源 0岗位二维码签约
     */
    private Integer source;
    /**
     * 根据数据来源关联业务id
     */
    private Long sourceBusinessId;


    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 签约时间开始
     */
    private Timestamp signDateStart;
    /**
     * 签约时间结束
     */
    private Timestamp signDateEnd;
    /**
     * 合同类型
     */
    private Integer type;
    /**
     * 合同类型集合
     */
    private List<Integer> types;
    /**
     * 合同状态
     */
    private Integer status;

    /**
     * 合同状态
     */
    private List<Integer> statusList;


    /**
     * 第三方业务应答id
     */
    private List<String> serviceBusinessIds;


    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 合同主体id
     */
    private Long subjectId;

    /**
     * 创建开始时间
     */
    private Timestamp createTimeStart;

    /**
     * 创建结束时间
     */
    private Timestamp createTimeEnd;

}
