package cn.xunhou.web.xbbcloud.product.sign.result;

import lombok.Data;

import java.util.List;

/**
 * 人力岗位列表及项目信息
 */
@Data
public class PostAndProjectResult {

    /**
     * 岗位ID
     */
    private Long postId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 工作地点
     */
    private String areaCode;

    /**
     * 业务类型
     * UNKNOWN_BUSINESS_TYPE = 0;
     * //全职外包
     * FULL_TIME_OUTSOURCING = 1;
     * //兼职外包
     * PART_TIME = 3;
     * //实习外包
     * TRAINEE = 4;
     * //转移外包
     * TRANSFER_OUTSOURCING = 5;
     * //RPO月返
     * RPO_MONTH = 7;
     * //人事代理
     * PERSONNEL_AGENCY = 8;
     * //劳务派遣
     * LABOR_DISPATCH = 10;
     * //实习(寒假工)
     * TRAINEE_WINTER_VACATION = 11;
     * //实习(暑假工)
     * TRAINEE_SUMMER_VACATION = 12;
     * //一次性RPO
     * ONCE_RPO = 13;
     * //平台RPO
     * PLATFORM_RPO = 14;
     * //灵活用工
     * FLEXIBLE_EMPLOYMENT = 15;
     * //代征代缴
     * COLLECTION_AND_PAYMENT = 16;
     * //钉钉RPO
     * DINGDING_RPO = 18;
     * //劳务外包
     * LABOR_OUTSOURCING = 19;
     * //BPO/其他
     * BPO_OR_OTHER = 99;
     */
    private Integer businessType;
    /**
     * 客户ID
     */
    private Long customerId;

    private List<SubjectConfigurationResult> subjectConfigurationDtoList;
}
