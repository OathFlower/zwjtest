package cn.xunhou.xbbcloud.rpc.sign.pojo.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;


@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PositionQrcodeResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
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

    /**
     * 删除标识
     */
    private Integer deletedFlag;
    /**
     * 创建时间
     */
    private Timestamp createdAt;
    /**
     * 更新时间
     */
    private Timestamp updatedAt;
}
