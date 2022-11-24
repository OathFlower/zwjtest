package cn.xunhou.xbbcloud.middleware.rocket.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class XcyPayrollMessage {

    /**
     * 业务id
     */
    @NotNull(message = "来源id不能为空")
    private Long sourceId;

    /**
     * mq批次号
     */
    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    /**
     * 发薪账户
     */
    @NotNull(message = "发薪账户不能为空")
    private String payrollWxSubMchid;


    /**
     * 备注
     */
    private String remark;

    /**
     * 结算发薪明细
     */
    @NotEmpty(message = "发薪详情不能为空")
    private List<XcyPayrollMessageDto> xcyPayrollDetailDtoList;
}
