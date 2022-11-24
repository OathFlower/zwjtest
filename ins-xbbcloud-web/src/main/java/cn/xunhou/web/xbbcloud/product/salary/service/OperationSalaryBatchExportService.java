package cn.xunhou.web.xbbcloud.product.salary.service;

import cn.xunhou.cloud.task.core.AbstractXbbExportService;
import cn.xunhou.cloud.task.core.XbbTableTemplate;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryBatchPageParam;
import cn.xunhou.web.xbbcloud.product.salary.result.OperationSalaryBatchExportData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XbbTableTemplate(templateName = "OperationSalaryBatchExportService")
public class OperationSalaryBatchExportService extends AbstractXbbExportService<OperationSalaryBatchExportData, SalaryBatchPageParam> {

    @Autowired
    private SalaryService salaryService;

    @Override
    public List<OperationSalaryBatchExportData> exportData(List<String> list, SalaryBatchPageParam param, boolean b, int i, int i1) {

        List<OperationSalaryBatchExportData> exportData = salaryService.getOperationSalaryBatchData(param);
        if (CollectionUtils.isEmpty(exportData)) {
            List<OperationSalaryBatchExportData> headEmptyList = new ArrayList<>(); //空表头list
            headEmptyList.addAll(Arrays.asList(new OperationSalaryBatchExportData()));
            return headEmptyList;
        }
        return exportData;
    }

}
