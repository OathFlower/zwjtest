package cn.xunhou.xbbcloud.rpc.other.pojo.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class PageBaseParam implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer page;
    private Integer pageSize;
}
