package cn.xunhou.web.xbbcloud.config.xhrpc.r;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XhP {
    /**
     * 跟踪请求链路随机唯一标示
     */
    private String traceId;
    /**
     * 跟踪请求逻辑区标示
     */
    private String area;
    /**
     * 自定义扩展透传字段
     */
    private Map<String, Object> transmitExtend;

    /**
     * 指定当前用户id，可以传0
     */
    private Long currentUserId;

    /**
     * 客户端id
     */
    private Long clientId;
    /**
     * 请求入参
     */
    private String data;

    /**
     * cat统计
     */
    private Map<String, Object> cat;

    /**
     * 初始化根域名
     */
    private String rootDomain;

    /**
     * 初始请求url
     */
    private String initiateUrl;

    /**
     * 请求发起时间
     */
    private Long timeRiver;

    /**
     * 版本号
     */
    private String version;

    /**
     * 服务ip
     */
    private String originalIp;
}
