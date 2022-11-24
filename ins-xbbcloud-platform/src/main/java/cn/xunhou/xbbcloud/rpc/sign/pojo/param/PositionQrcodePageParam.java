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
public class PositionQrcodePageParam extends PageBaseParam {


    //搜索创建时间起
    private Timestamp createDateStart;

    //搜索创建时间截止
    private Timestamp createDateEnd;

    //人力岗位idList
    private List<Long> hroPositionIds;

    /**
     * 租户id
     */
    private Long tenantId;
}
