package cn.xunhou.web.xbbcloud.product.sxz.controller;

import cn.xunhou.cloud.core.page.PageInfo;
import cn.xunhou.cloud.core.web.JsonListResponse;
import cn.xunhou.cloud.core.web.JsonResponse;
import cn.xunhou.web.xbbcloud.product.sxz.dto.ReceiptRecordResult;
import cn.xunhou.web.xbbcloud.product.sxz.param.SaveReceiptParam;
import cn.xunhou.web.xbbcloud.product.sxz.service.ReceiptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 省薪招-发票相关
 */
@RequestMapping("/api/receipt")
@Slf4j
@RestController
public class ReceiptController {

    @Autowired
    private ReceiptService receiptService;

    /**
     * 新增发票
     *
     * @param param
     * @return
     */
    @PostMapping
    public JsonResponse save(@RequestBody @Validated SaveReceiptParam param) {
        try {
            return receiptService.saveReceipt(param);
        } catch (Exception e) {
            return JsonResponse.systemError(e.getMessage());
        }
    }

        /**
         * 开票记录
         *
         * @param param
         * @return
         */
        @GetMapping("/records")
    public JsonListResponse<ReceiptRecordResult> record(PageInfo param) {
        return receiptService.record(param);
    }
}
