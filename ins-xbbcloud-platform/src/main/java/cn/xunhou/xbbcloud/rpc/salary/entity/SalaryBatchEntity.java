package cn.xunhou.xbbcloud.rpc.salary.entity;

import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeTenantEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@XbbTable(table = "salary_batch")
public class SalaryBatchEntity extends XbbSnowTimeTenantEntity {

    /**
     * 项目id
     */
    private Long productId;

    /**
     * 计薪年月 yyyymm
     */
    private String month;

    /**
     * 状态  0进行中 1全部成功 2部分成功 3全部失败
     */
    private Integer status;

    /**
     * 来源 0SASS 1代发
     */
    private Integer source;

    /**
     * 操作人id
     */
    private Long operatorId;

    /**
     * 备注
     */
    private String remarks;


    /**
     * 发薪文件
     */

    private String salaryFile;

    /**
     * 发薪主体id
     */
    private Long subjectId;

    /**
     * 扩展数据json
     */
    private String expandJson;
    /**
     * 发薪方式 1小程序提现  2微信转账
     */
    private Integer payMethod;
}
