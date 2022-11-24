package cn.xunhou.web.xbbcloud.product.sxz.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;


@ToString
@Data
@Accessors(chain = true)
public class RecommendResult {
    private Long id;
    /**
     * 二维码url
     */
    private String qrcodeUrl;
    /**
     * 邀请码
     */
    private String interviewCode;
}
