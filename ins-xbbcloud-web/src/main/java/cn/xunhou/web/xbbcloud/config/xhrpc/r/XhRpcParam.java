package cn.xunhou.web.xbbcloud.config.xhrpc.r;

import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumProject;
import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XhRpcParam {
    /**
     * 服务端项目（必填）
     */
    private EnumProject serviceProject;
    /**
     * rpc接口地址 /IStaffService/listStaff （必填）
     */
    private String uri;
    /**
     * 请求入参
     */
    private Object request;

    /**
     * 请求人id 默认0L
     */
    private Long userId;

    /**
     * 查询租户
     */
    private EnumXhTenant xhTenant;

    /**
     * 客户端id (非必填，默认当前项目clientId)
     */
    private Long clientId;


}
