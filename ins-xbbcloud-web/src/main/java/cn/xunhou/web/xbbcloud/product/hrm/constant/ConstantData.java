package cn.xunhou.web.xbbcloud.product.hrm.constant;

import cn.hutool.poi.excel.ExcelUtil;
import cn.xunhou.cloud.core.util.SystemUtil;

/**
 * @author sha.li
 * @since 2022/9/14
 */
public interface ConstantData {
    /**
     * 员工导入模板路径
     */
    String EMPLOYEE_IMPORT_TEMPLATE_PATH = "hrm/excel.template/导入员工模板.xlsx";


    /**
     * 员工导入模板路径
     */
    String SALARY_IMPORT_TEMPLATE_PATH = "hrm/excel.template/导入发薪批次模板.xlsx";

    /**
     * xlsx的ContentType
     */
    String XLSX_CONTENT_TYPE = ExcelUtil.XLSX_CONTENT_TYPE;

    /**
     * 1MB的字节数
     */
    long ONE_MB_BYTES = 1024 * 1024;

    /**
     * 员工导入模板最大记录数
     */
    int EMPLOYEE_IMPORT_TEMPLATE_MAX_ROW = 1000;

    String ACTIVATE_MOBILE_EMPLOYEE = "ACTIVATE_MOBILE_EMPLOYEE";

    /**
     * 前端静态资源文件cdn域名
     */
    String END_FRONT_STATIC_RESOURCE_URL_PREFIX = SystemUtil.isOffline() ? "https://qa-static.xunhou.cn/%s" : "https://static.xunhou.cn/%s";
}
