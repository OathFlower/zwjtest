
package cn.xunhou.xbbcloud.rpc.other.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * ENGINE=InnoDB default charset=utf8mb4 comment='字典表'
 *
 * @author system
 * @since 2022-08-03 16:03:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "dictionary")
public class DictionaryEntity extends XbbSnowTenantEntity {


    /**
     * 类型 1用工类型 2招聘渠道
     */
    private Integer type;

    /**
     * 父级编码
     */
    private Integer parentCode;

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 排序值
     */
    private Integer sort;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long modifyBy;

}
