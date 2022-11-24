package cn.xunhou.web.xbbcloud.product.salary.service;

import cn.xunhou.cloud.task.core.AbstractXbbExportService;
import cn.xunhou.cloud.task.core.XbbTableTemplate;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryDetailPageParam;
import cn.xunhou.web.xbbcloud.product.salary.result.OperationSalaryDetailExportData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XbbTableTemplate(templateName = "OperationSalaryDetailExportService")
public class OperationSalaryDetailExportService extends AbstractXbbExportService<OperationSalaryDetailExportData, SalaryDetailPageParam> {

    @Autowired
    private SalaryService salaryService;

    @Override
    public List<OperationSalaryDetailExportData> exportData(List<String> list, SalaryDetailPageParam param, boolean b, int i, int i1) {

        List<OperationSalaryDetailExportData> exportData = salaryService.getOperationSalaryDetailData(param);
        if (CollectionUtils.isEmpty(exportData)) {
            List<OperationSalaryDetailExportData> headEmptyList = new ArrayList<>(); //空表头list
            headEmptyList.addAll(Arrays.asList(new OperationSalaryDetailExportData()));
            return headEmptyList;
        }
        return exportData;
    }

}
