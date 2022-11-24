package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author wangkm
 */
@Getter
@Setter
@Accessors(chain = true)
public class Xcy2XbbBackDetailMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 详情id
     */
    private Long detailId;
}
