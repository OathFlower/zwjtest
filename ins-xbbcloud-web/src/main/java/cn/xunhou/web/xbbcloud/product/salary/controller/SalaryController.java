package cn.xunhou.web.xbbcloud.product.salary.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.hrm.param.ImportSalaryRowData;
import cn.xunhou.web.xbbcloud.product.hrm.result.ImportSalaryResult;
import cn.xunhou.web.xbbcloud.product.hrm.service.ExcelService;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryBatchPageParam;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryBatchSaveParam;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryDetailPageParam;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryMerchantFlowPageParam;
import cn.xunhou.web.xbbcloud.product.salary.result.*;
import cn.xunhou.web.xbbcloud.product.salary.service.SalaryService;
import cn.xunhou.web.xbbcloud.util.MvcUtil;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 薪酬云相关
 *
 * @author wkm
 */
@RestController
@RequestMapping("/api/salaries")
@Validated
public class SalaryController {

    @Resource
    private SalaryService salaryService;

    @Autowired
    ExcelService excelService;
    @Autowired
    HttpServletResponse response;

    @ModelAttribute
    public void check(HttpServletRequest request, HttpServletResponse response) {
        //下列接口不校验商户号薪酬云功能开启和关闭
        List<String> notFilter = Lists.newArrayList(
                "/api/salaries/excel/template",
                "/api/salaries/merchant"
        );
        String uri = request.getRequestURI();
        if (!notFilter.contains(uri)) {
            salaryService.checkMerchantIsUse();
        }
    }


    /**
     * 新增薪资导入
     */
    @PostMapping("/excel/import")
    public JsonResponse<ImportSalaryResult<ImportSalaryRowData>> importSalary(@RequestParam("file") MultipartFile file) {
        return JsonResponse.success(excelService.importSalary(file));
    }

    /**
     * 下载薪资导入的模板
     */
    @GetMapping("/excel/template")
    public void downloadImportTemplate() throws IOException {
        try (Workbook workbook = excelService.getSalaryImportTemplateWorkbook(); OutputStream outputStream = response.getOutputStream()) {
            MvcUtil.setExcelResponseInfo(response, "导入发薪批次模板.xlsx");
            workbook.write(outputStream);
            outputStream.flush();
        }
    }

    /**
     * 导出
     *
     * @param param
     * @return
     */
    @GetMapping("/excel/export")
    public JsonResponse<String> export(SalaryDetailPageParam param) {
        return salaryService.export(param);
    }

    /**
     * 查询商户信息
     *
     * @return 商户信息
     */
    @GetMapping("/merchant")
    public JsonResponse<SalaryMerchantInfoResult> merchantInfo() {
        return JsonResponse.success(salaryService.merchantInfo());
    }

    /**
     * 查询商户余额
     *
     * @return 商户信息
     */
    @GetMapping("/merchant/balance")
    public JsonResponse<SalaryMerchantInfoResult> findMerchantBalance() {
        return JsonResponse.success(salaryService.findMerchantBalance(null));
    }

    /**
     * 商户交易流水查询（操作金额的相关逻辑）
     *
     * @param param 查询条件
     * @return 分页流水
     */
    @PostMapping("/merchant/flow")
    public JsonListResponse<SalaryMerchantFlowResult> findMerchantFlow(@Validated @RequestBody SalaryMerchantFlowPageParam param) {
        return salaryService.findMerchantFlow(param);
    }


    /**
     * 分页查询批次列表
     *
     * @param param 查询条件
     * @return
     */
    @PostMapping("/batch/records")
    public JsonListResponse<SalaryBatchResult> findSalaryBatchPageList(@Validated @RequestBody SalaryBatchPageParam param) {
        return salaryService.findSalaryBatchPageList(param, false);
    }

    /**
     * 分页查询明细列表
     *
     * @param param 查询条件
     * @return
     */
    @PostMapping("/detail/records")
    public JsonListResponse<SalaryDetailResult> findSalaryDetailPageList(@Validated @RequestBody SalaryDetailPageParam param) {
        return salaryService.findSalaryDetailPageList(param, false);
    }

    /**
     * 项目列表
     *
     * @return
     */
    @GetMapping("/product")
    public JsonResponse<List<SalaryProductResult>> querySalaryProduct() {

        return salaryService.querySalaryProduct();
    }


    /**
     * 保存发薪批次
     *
     * @param param 入参
     * @return 结果
     */
    @PostMapping("/batch")
    public JsonResponse<SalaryBatchResult> saveSalaryBatch(@Validated @RequestBody SalaryBatchSaveParam param) {
        return salaryService.saveSalaryBatch(param);
    }
}
