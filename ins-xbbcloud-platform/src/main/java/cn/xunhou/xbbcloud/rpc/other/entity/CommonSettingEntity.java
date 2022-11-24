
package cn.xunhou.xbbcloud.rpc.other.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * comment '通用设置表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "common_setting")
public class CommonSettingEntity extends XbbSnowTimeTenantEntity {


    /**
     * 配置类型 0默认 1排班预警
     */
    private Integer type;

    /**
     * 配置信息json字符串
     */
    private String configInfo;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifyBy;

}
