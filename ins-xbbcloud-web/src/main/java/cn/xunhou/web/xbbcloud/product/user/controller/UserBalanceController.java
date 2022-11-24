package cn.xunhou.web.xbbcloud.product.user.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceDetailResult;
import cn.xunhou.web.xbbcloud.product.user.param.UserBalanceQueryParam;
import cn.xunhou.web.xbbcloud.product.user.result.UserBalanceResult;
import cn.xunhou.web.xbbcloud.product.user.service.UserBalanceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户余额相关
 */
@RestController
@RequestMapping("/user/balance")
public class UserBalanceController {

    @Resource
    private UserBalanceService userBalanceService;

    /**
     * 用户余额列表查询
     * @param param 查询条件
     * @return 列表数据
     */
    @PostMapping("/page")
    public JsonListResponse<UserBalanceResult> page(@Validated @RequestBody UserBalanceQueryParam param){
        return userBalanceService.page(param) ;
    }

    /**
     * 流水明细
     * @param userXhCId 用户id
     * @return 流水明细
     */
    @PostMapping("/detail/{userXhCId}")
    public JsonListResponse<UserBalanceDetailResult> detail(@PathVariable(value = "userXhCId") Long userXhCId){
        return userBalanceService.detail(userXhCId) ;
    }
}
