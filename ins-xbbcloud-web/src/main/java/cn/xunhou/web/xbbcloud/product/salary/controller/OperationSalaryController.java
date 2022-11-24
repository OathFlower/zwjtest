package cn.xunhou.web.xbbcloud.product.salary.controller;

import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.salary.param.*;
import cn.xunhou.web.xbbcloud.product.salary.result.*;
import cn.xunhou.web.xbbcloud.product.salary.service.SalaryService;
import cn.xunhou.web.xbbcloud.util.pojo.result.ZipFilesResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 运营后台薪酬云相关
 *
 * @author wangkm
 */
@RestController
@RequestMapping("/api/salaries/operation")
@Validated
public class OperationSalaryController {

    @Resource
    private SalaryService salaryService;



    /**
     * 导出发薪批次
     *
     * @param param
     * @return
     */
    @GetMapping("/excel/export/batch")
    public JsonResponse<String> exportBatch(SalaryBatchPageParam param) {
        return salaryService.operationExportBatch(param);
    }

    /**
     * 导出发薪明细
     *
     * @param param
     * @return
     */
    @GetMapping("/excel/export/detail")
    public JsonResponse<String> exportDetail(SalaryDetailPageParam param) {
        return salaryService.operationExportDetail(param);
    }

    /**
     * 分页查询批次列表
     *
     * @param param 查询条件
     * @return
     */
    @PostMapping("/batch/records")
    public JsonListResponse<SalaryBatchResult> findSalaryBatchPageList(@Validated @RequestBody SalaryBatchPageParam param) {
        return salaryService.findSalaryBatchPageList(param, true);
    }

    /**
     * 分页查询明细列表
     *
     * @param param 查询条件
     * @return
     */
    @PostMapping("/detail/records")
    public JsonListResponse<SalaryDetailResult> findSalaryDetailPageList(@Validated @RequestBody SalaryDetailPageParam param) {
        return salaryService.findSalaryDetailPageList(param, true);
    }

    /**
     * 断点重新支付
     *
     * @param batchId 批次id
     */
    @PostMapping("/breakpoint/retry")
    public JsonResponse<Boolean> breakpointRetry(@RequestParam("batch_id") Long batchId) {
        salaryService.breakpointRetry(batchId);
        return JsonResponse.success(true);
    }


    /**
     * 资金撤回
     *
     * @param detailId 详情id
     */
    @GetMapping("/fund/back")
    public JsonResponse<Boolean> fundBack(@RequestParam("detail_id") Long detailId) {
        salaryService.fundBack(Collections.singleton(detailId));
        return JsonResponse.success(true);
    }


    /**
     * 回执单下载
     *
     * @param param 提现ids
     */
    @PostMapping("/download/ack")
    public JsonResponse<ZipFilesResult> downloadAck(@Validated @RequestBody SalaryDownloadAckParam param) {
        return JsonResponse.success(salaryService.downloadAck(param));
    }

    /**
     * 主体账户流水
     *
     * @param param 查询入参
     * @return 查询结果
     */
    @PostMapping("/subject/flow")
    public JsonListResponse<SubjectFlowInfoResult> subjectFlow(@Validated @RequestBody SubjectFlowPageParam param) {
        return salaryService.subjectFlow(param);
    }

    /**
     * 查询子账户余额
     *
     * @param subAccountId 子账户id
     * @return 余额
     */
    @GetMapping("sub/account/balance")
    public JsonResponse<SubAccountBalanceResult> subAccountBalance(@RequestParam("sub_account_id") Long subAccountId) {
        return JsonResponse.success(salaryService.subAccountBalance(subAccountId));
    }

    /**
     * 子账户流水
     *
     * @param param 查询入参
     * @return 查询结果
     */
    @PostMapping("/sub/account/flow")
    public JsonListResponse<SubAccountFlowResult> subAccountFlow(@Validated @RequestBody SubAccountFlowPageParam param) {
        return salaryService.subAccountFlow(param);
    }

    /**
     * 主体
     *
     * @param queryKeyword 名称搜索
     * @return
     */
    @GetMapping("/subject/suggest")
    public JsonResponse<List<TextValueResult>> subjectSuggest(@RequestParam(value = "query_keyword", required = false) String queryKeyword) {
        return salaryService.subjectSuggest(queryKeyword);
    }


    /**
     * 子账号
     *
     * @param subjectId 主账号
     * @return
     */
    @GetMapping("/sub/account/suggest")
    public JsonResponse<List<TextValueResult>> subAccountSuggest(@RequestParam(value = "subject_id") Long subjectId) {
        return salaryService.subAccountSuggest(subjectId);
    }
}
