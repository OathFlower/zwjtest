package cn.xunhou.web.xbbcloud.product.user.param;

import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.cloud.dao.query.PageQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户余额查询条件
 */
@Getter
@Setter
@ToString
public class UserBalanceQueryParam {
    /**姓名*/
    private String name;
    /**手机号*/
    private String tel;
    /**身份证*/
    private String idCardNo;
    private Integer curPage = 0;
    private Integer pageSize = 10;

}
