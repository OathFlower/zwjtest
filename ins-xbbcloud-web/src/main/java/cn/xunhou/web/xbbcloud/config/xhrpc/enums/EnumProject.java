package cn.xunhou.web.xbbcloud.config.xhrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangkm
 */

@Getter
@AllArgsConstructor
public enum EnumProject {

    /**
     * "ins-hrostaff-platform","人力系统"
     */
    HROSTAFF("ins-hrostaff-platform", "人力系统", 10235L),

    /**
     * "ins-xbb-platform","xbb系统"contractlist
     */
    XBB("ins-xbb-platform", "xbb系统", 10327L),

    /**
     * "ins-userxh-platform","用户客户信息"
     */
    USERXH("ins-userxh-platform", "用户客户信息", 20312L),

    /**
     * "ins-starpro-web","交付系统",
     */
    STARPRO_WEB("ins-starpro-web", "交付系统", 20286L),
    ;
    /**
     * 项目名
     */
    private final String projectName;
    /**
     * 描述
     */
    private final String describe;

    /**
     *
     */
    private final Long clientId;
}
