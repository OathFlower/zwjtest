package cn.xunhou.xbbcloud.rpc.sign.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "contract")
public class ContractEntity extends XbbSnowTimeEntity {


    /**
     * 上上签合同、协议模板id
     */
    private Long templateId;
    /**
     * 员工id
     */
    private Long employeeId;
    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 签署主体id
     */
    private Long subjectId;
    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 类型 1合同，2协议
     */
    private Integer type;
    /**
     * 编号
     */
    private String contractNo;


    /**
     * 签约日期
     */
    private Timestamp signDate;
    /**
     * 状态 「未签署」、「待确认」、「待生效」、「生效中」、「已到期」、「已作废」、「提前终止」
     */
    private Integer status;

    /**
     * 数据来源 0岗位二维码签约
     */
    private Integer source;
    /**
     * 根据数据来源关联业务id
     */
    private Long sourceBusinessId;
    /**
     * 模板json
     */
    private String templateJson;

    /**
     * 合同开始时间
     */
    private Timestamp startTime;
    /**
     * 合同截止时间
     */
    private Timestamp endTime;

    /**
     * 第三方相应id
     */
    private String serviceBusinessId;


    /**
     * 电子合同文件系统id
     */
    private String contractOssId;



}
