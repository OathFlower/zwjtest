package cn.xunhou.web.xbbcloud.product.salary.param;

import cn.xunhou.cloud.core.page.PageInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SalaryDetailPageParam extends PageInfo {
    /**
     * 项目名称
     */
    private String productName;

    /**
     * 员工姓名
     */
    private String staffName;

    /**
     * 发薪日期 开始 对应创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String startSubmitTime;

    /**
     * 发薪日期 截至 对应创建时间 yyyy-MM-dd HH:mm:ss
     */
    private String endSubmitTime;


    /**
     * 更新时间开始 yyyy-MM-dd HH:mm:ss
     */
    private String updateTimeStart;

    /**
     * 更新时间截至 yyyy-MM-dd HH:mm:ss
     */
    private String updateTimeEnd;

    /**
     * 身份证号
     */
    private String idCardNo;
    /**
     * 手机号
     */
    private String phone;

    /**
     * 明细状态
     */
    private List<Integer> detailStatusList;

    /**
     * 批次编号
     */
    private Long batchId;


    /**
     * 租户id
     */
    private Long tenantId;


}
