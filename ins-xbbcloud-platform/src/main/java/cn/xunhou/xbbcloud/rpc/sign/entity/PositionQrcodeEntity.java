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
@XbbTable(table = "position_qrcode")
public class PositionQrcodeEntity extends XbbSnowTimeEntity {


    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 人力岗位ID
     */
    private Long hroPositionId;

    /**
     * 0不缴纳社保 1缴纳社保
     */
    private Integer socialInsurance;
    /**
     * 合同模板类型
     */
    private Integer contractTemplateType;
    /**
     * 企业动态表单json
     */
    private String templateJson;
    /**
     * 二维码过期时间
     */
    private Timestamp expireDate;
    /**
     * 企业合同主体id
     */
    private Long subjectId;
    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人id
     */
    private Long operatorId;


}
