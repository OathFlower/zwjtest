package cn.xunhou.xbbcloud.rpc.approve.config;

import lombok.Data;

import java.util.Map;

/**
 * @Author: chenning
 * @Date: 2022/10/12/21:07
 * @Description:
 */
@Data
public class FormDetail {

    /**
     * 字段v0-v9
     */
    private String column;

    /**
     * 字段名
     */
    private String label;
    /**
     * 位置(table用)
     */
    private Integer index = 0;
    /**
     * 是否显示(table用)
     */
    private boolean displayFlag = true;
    /**
     * 审批状态(table用)
     */
    private Integer status = 0;
    /**
     * 审批类型(form用)
     */
    private Integer applyType = 0;
    /**
     *  字段类型是否需要根据相关查找value (form用)0 其他，1，推荐官  2:ID，2.组织机构，3. 角色 4:accountId
     */
    private Integer fieldType ;
    /**
     * 枚举标识(form用)
     */
    private Map<String,String> enumValue ;

    private boolean controlFlag = false;

    private Integer maskingType;

    private String field = "";

    private String type = "";
}
