package cn.xunhou.web.xbbcloud.product.hrm.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @program: ins-xhportal-web
 * @description: MenusPageListParam
 * @author: jiang_tian
 * @create: 2022-08-04 15:42
 **/
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class RoleIdListParam {

    @NotEmpty(message = "roleIdList must not be null")
    private List<Long> roleIdList;


}
