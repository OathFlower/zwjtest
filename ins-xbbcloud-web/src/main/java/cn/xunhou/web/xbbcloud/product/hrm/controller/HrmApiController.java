package cn.xunhou.web.xbbcloud.product.hrm.controller;

import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.hrm.param.HrmSaveTenantParam;
import cn.xunhou.web.xbbcloud.product.hrm.service.ExcelService;
import cn.xunhou.web.xbbcloud.product.hrm.service.HrmEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 人事云-api接口
 *
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
@RestController
@RequestMapping("/hrm/api")
public class HrmApiController {

    @Autowired
    ExcelService excelService;
    @Autowired
    HttpServletResponse response;

    @Autowired
    private HrmEmployeeService employeeService;

    /**
     * 企业创建并生成 主账号 角色 部门
     *
     * @param param
     * @return
     */
    @PostMapping("tenant/save")
    public JsonResponse<?> saveTenant(@RequestBody @Validated HrmSaveTenantParam param) {
        return employeeService.saveTenant(param);
    }

}
