package cn.xunhou.xbbcloud.rpc.sign.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class QueryPositionUserParam {
    /**
     * 岗位二维码id
     */
    private Long positionQrcodeId;

}
