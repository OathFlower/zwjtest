package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.sxz.dto.AccountInfoResult;
import cn.xunhou.web.xbbcloud.product.sxz.dto.AccountMultipleResult;
import cn.xunhou.web.xbbcloud.product.sxz.param.AccountMultiplePageParam;
import cn.xunhou.web.xbbcloud.product.sxz.service.AccountService;
import cn.xunhou.web.xbbcloud.product.sxz.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 省薪招-用户相关
 */
@RequestMapping("/api/users")
@Slf4j
@RestController
public class UserController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;


    /**
     * 账户信息列表 余额,开票金额,剩余班点等
     *
     * @param param
     * @return
     */
    @GetMapping
    public JsonListResponse<AccountMultipleResult> list(AccountMultiplePageParam param) {
        return userService.list(param);
    }

    /**
     * 账户信息
     *
     * @return
     */
    @GetMapping("/detail")
    public JsonResponse<AccountInfoResult> info() {
        return userService.info();
    }


}
