package cn.xunhou.web.xbbcloud.product.salary.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryBatchSaveParam extends PageInfo {
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 计薪年月 yyyymm
     */
    private String month;


    /**
     * 文件名
     */
    private String salaryFile;


}
