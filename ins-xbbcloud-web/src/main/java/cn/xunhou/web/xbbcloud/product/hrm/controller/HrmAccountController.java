package cn.xunhou.web.xbbcloud.product.hrm.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.hrm.param.EmployeeDictionarySaveParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.RoleIdListParam;
import cn.xunhou.web.xbbcloud.product.hrm.result.EmployeeDictionaryResult;
import cn.xunhou.web.xbbcloud.product.hrm.result.ImportResult;
import cn.xunhou.web.xbbcloud.product.hrm.service.ExcelService;
import cn.xunhou.web.xbbcloud.product.hrm.service.HrmAccountService;
import cn.xunhou.web.xbbcloud.product.hrm.service.HrmEmployeeService;
import cn.xunhou.web.xbbcloud.util.MvcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 人事云-员工
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
@RestController
@RequestMapping("/hrm/account")
public class HrmAccountController {

    @Autowired
    ExcelService excelService;
    @Autowired
    HttpServletResponse response;

    @Autowired
    private HrmEmployeeService employeeService;

    @Autowired
    private HrmAccountService accountService;

    /**
     * 通过账户id查询已分配的角色id
     *
     * @param accountId
     * @return
     */
    @GetMapping("/{system}/{account_id}/roles")
    public JsonResponse<List<String>> findAccountRole(@PathVariable("system") String system,
                                                      @PathVariable("account_id") Long accountId) {
        return accountService.findAccountRole(accountId);
    }


    /**
     * 删除->角色和账户关联关系
     *
     * @param accountId
     * @return
     */
    @PostMapping("/{system}/{account_id}/unbinding/role")
    public JsonResponse<?> deleteRolesByAccountId(@PathVariable("system") String system
            , @PathVariable("account_id") Long accountId
            , @RequestParam("role_id") Long roleId) {
        return accountService.deleteRolesByAccountId(accountId, roleId);
    }


    /**
     * 新增->角色和账户关联关系
     *
     * @param param
     * @return
     */
    @PostMapping("/{system}/{account_id}/binding/roles")
    public JsonResponse<?> saveRolesByAccountId(@PathVariable("system") String system, @PathVariable("account_id") Long accountId, @RequestBody @Validated RoleIdListParam param) {
        return accountService.saveRolesByAccountId(accountId, param);
    }

}
