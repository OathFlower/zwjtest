package cn.xunhou.web.xbbcloud.product.sxz.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 核销记录入参
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class VerificationRecordParam extends PageInfo {
    /**
     * 用户id
     */
    private Long userId;


}
