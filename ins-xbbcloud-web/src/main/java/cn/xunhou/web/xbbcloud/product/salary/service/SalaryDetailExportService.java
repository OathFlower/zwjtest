package cn.xunhou.web.xbbcloud.product.salary.service;

import cn.xunhou.cloud.task.core.AbstractXbbExportService;
import cn.xunhou.cloud.task.core.XbbTableTemplate;
import cn.xunhou.web.xbbcloud.product.salary.param.SalaryDetailPageParam;
import cn.xunhou.web.xbbcloud.product.salary.result.SalaryDetailExportData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XbbTableTemplate(templateName = "SalaryDetailExportService")
public class SalaryDetailExportService extends AbstractXbbExportService<SalaryDetailExportData, SalaryDetailPageParam> {

    @Autowired
    private SalaryService salaryService;

    @Override
    public List<SalaryDetailExportData> exportData(List<String> list, SalaryDetailPageParam param, boolean b, int i, int i1) {

        List<SalaryDetailExportData> exportData = salaryService.getExportData(param);
        if (CollectionUtils.isEmpty(exportData)) {
            List<SalaryDetailExportData> headEmptyList = new ArrayList<>(); //空表头list
            headEmptyList.addAll(Arrays.asList(new SalaryDetailExportData()));
            return headEmptyList;
        }
        return exportData;
    }

}
