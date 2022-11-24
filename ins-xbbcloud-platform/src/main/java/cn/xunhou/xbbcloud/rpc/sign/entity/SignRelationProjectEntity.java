package cn.xunhou.xbbcloud.rpc.sign.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@XbbTable(table = "sign_relation_project")
public class SignRelationProjectEntity extends XbbSnowTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 签约云-基本信息ID
     */
    private Long signInfoId;
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 备注
     */
    private String remarks;

    /**
     * 操作人
     */
    private Long operatorId;
}
