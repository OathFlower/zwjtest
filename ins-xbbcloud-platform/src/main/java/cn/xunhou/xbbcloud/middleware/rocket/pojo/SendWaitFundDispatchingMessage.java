package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import cn.xunhou.xbbcloud.rpc.salary.entity.SalaryDetailEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;


@Data
@ToString
@NoArgsConstructor
public class SendWaitFundDispatchingMessage implements Serializable {

    private List<SalaryDetailEntity> salaryDetailEntityList;

    private String batchId;
    //延时时间
    private Long delayTime;

    private String specialMerchantId;
    private String payeeMerchantName;
    private String payeeMerchantNo;
    private Boolean xcxWithdraw;
    private Integer tenantType;

    private Integer count;
}
