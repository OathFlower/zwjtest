package cn.xunhou.xbbcloud.rpc.approve.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: chenning
 * @Date: 2022/09/27/16:06
 * @Description:
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "workflow_form_template")
public class WorkflowFormEntity extends XbbSnowTimeTenantEntity {


    /**
     * 审批实例表id
     */
    private Long insId;

    /**
     * 类型
     */
    private Long flowTempId;

    /**
     * 类型
     */
    private Long formTempId;

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

    /**
     * 表单Json
     */
    private String formJson;

    /**
     * 新数据json
     */
    private String newJson;

}
