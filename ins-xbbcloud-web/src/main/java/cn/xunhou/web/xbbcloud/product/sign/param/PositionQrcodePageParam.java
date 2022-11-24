package cn.xunhou.web.xbbcloud.product.sign.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PositionQrcodePageParam extends PageInfo {
    /**
     * 岗位名称
     */
    private String positionName;
    /**
     * 搜索创建时间起止  yyyy-MM-dd HH:mm:ss
     */
    private String createDateStart;
    /**
     * 搜索创建时间截止 yyyy-MM-dd HH:mm:ss
     */
    private String createDateEnd;

}
