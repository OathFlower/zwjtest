package cn.xunhou.web.xbbcloud.config.xhrpc.r;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XhTotal<U> {
    private Long totalCount;
    private List<U> list;
}
