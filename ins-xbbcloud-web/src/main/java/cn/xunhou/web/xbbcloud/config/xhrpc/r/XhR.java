package cn.xunhou.web.xbbcloud.config.xhrpc.r;

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
public class XhR<T> {
    private Integer status;
    private String message;
    private T data;
}
