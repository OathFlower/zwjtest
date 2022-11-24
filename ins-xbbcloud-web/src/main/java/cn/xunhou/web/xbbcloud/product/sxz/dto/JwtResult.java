package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author fico
 */
@Data
@Builder
public class JwtResult {
    /**
     * authorization token
     */
    private String token;

    /**
     * 有效期
     */
    private int expireTime;
}
