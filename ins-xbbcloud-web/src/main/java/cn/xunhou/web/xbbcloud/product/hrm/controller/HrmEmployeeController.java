package cn.xunhou.web.xbbcloud.product.hrm.controller;

import cn.hutool.core.util.StrUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.hrm.param.EmployeeDictionarySaveParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportStaffEmployeeCheckQueryParam;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportStaffEmployeeSubmitParam;
import cn.xunhou.web.xbbcloud.product.hrm.result.ContractResult;
import cn.xunhou.web.xbbcloud.product.hrm.result.EmployeeDictionaryResult;
import cn.xunhou.web.xbbcloud.product.hrm.result.ImportStaffEmployeeCheckResult;
import cn.xunhou.web.xbbcloud.product.hrm.service.ExcelService;
import cn.xunhou.web.xbbcloud.product.hrm.service.HrmEmployeeService;
import cn.xunhou.web.xbbcloud.util.MvcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 人事云-员工
 *
 * @author sha.li
 * @since 2022/9/14
 */
@Slf4j
@RestController
@RequestMapping("/hrm/employee")
public class HrmEmployeeController {

    @Autowired
    ExcelService excelService;
    @Autowired
    HttpServletResponse response;
    @Autowired
    private HrmEmployeeService employeeService;
    private static final XbbUserContext XBB_USER_CONTEXT = XbbUserContext.newSingleInstance();
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 下载员工导入的模板
     */
    @GetMapping("/excel/import/template")
    public void downloadImportTemplate() throws IOException {
        Long currentUserId = XBB_USER_CONTEXT.get().getUserId();
        RLock lock = redissonClient.getLock("hrmEmployee:downloadImportTemplate:" + currentUserId);
        try {
            if (!lock.tryLock()) {
                throw new SystemRuntimeException("导入员工模板正在下载中，请稍后再试");
            }
            try (Workbook workbook = excelService.getImportTemplateWorkbook(); OutputStream outputStream = response.getOutputStream()) {
                MvcUtil.setExcelResponseInfo(response, "导入员工模板.xlsx");
                workbook.write(outputStream);
                outputStream.flush();
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 员工新增导入 状态码为40301时，获取data中的数据为校验失败的原因下载链接
     */
    @PostMapping("/excel/import")
    public JsonResponse<String> importEmployee(@RequestParam("file") MultipartFile file) {
        Long currentUserId = XBB_USER_CONTEXT.get().getUserId();
        RLock lock = redissonClient.getLock("hrmEmployee:importEmployee:" + currentUserId);
        try {
            if (!lock.tryLock()) {
                throw new SystemRuntimeException("正在处理导入数据，请待导入完成后再试");
            }
            String fileDownloadUrl = excelService.importEmployee(file);
            if (StrUtil.isBlank(fileDownloadUrl)) {
                return JsonResponse.success();
            }
            return JsonResponse.<String>builder().code(40301).data(fileDownloadUrl).build();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 发薪2.0-拉取人力员工导入检查
     *
     * @return {@link JsonResponse}<{@link ImportStaffEmployeeCheckResult}>
     */
    @PostMapping("/staff/import/check")
    public JsonResponse<ImportStaffEmployeeCheckResult> importStaffEmployeeCheck(@RequestBody ImportStaffEmployeeCheckQueryParam param) {
        return employeeService.importStaffEmployeeCheck(param);
    }

    /**
     * 发薪2.0-拉取人力员工导入提交
     *
     * @param param
     * @return {@link JsonResponse}<{@link ?}>
     */
    @PostMapping("/staff/import/submit")
    public JsonResponse<?> importStaffEmployeeSubmit(@RequestBody ImportStaffEmployeeSubmitParam param) {
        return employeeService.importStaffEmployeeSubmit(param);
    }


    /**
     * 发薪2.0-获取员工下合同
     *
     * @param employeeId 雇员id
     * @return {@link JsonResponse}<{@link ?}>
     */
    @GetMapping("/contract/{employee_id}")
    public JsonListResponse<ContractResult> findContractByEmployeeId(@PathVariable("employee_id") @NotNull Long employeeId) {
        return JsonListResponse.success(employeeService.findContractByEmployeeId(employeeId));
    }
    /**
     * 员工管理->激活员工(给员工发短信)
     *
     * @return 结果
     */
    @PostMapping("/activate")
    public JsonResponse<?> activateMobileEmployee() {
        return employeeService.activateMobileEmployee();
    }


    /**
     * 员工管理->查看当日是否已给员工发送激活短信 true已发送过
     */
    @GetMapping("/isActivate")
    public JsonResponse<Boolean> isActivate() {
        return employeeService.isActivate();
    }


    /**
     * 员工字典保存
     *
     * @param param 参数
     * @return 主键id
     */
    @PostMapping("/dict")
    public JsonResponse<Long> saveDict(@RequestBody @Validated EmployeeDictionarySaveParam param) {
        return JsonResponse.success(employeeService.saveDict(param));
    }

    /**
     * 员工相关字典列表
     *
     * @param type 字典类型 1用工类型 2用工来源
     * @return 结果
     */
    @GetMapping("/dict/{type}/list")
    public JsonListResponse<EmployeeDictionaryResult> dictList(@PathVariable("type") Integer type) {
        return JsonListResponse.success(employeeService.findEmployeeDictList(type));
    }

}
