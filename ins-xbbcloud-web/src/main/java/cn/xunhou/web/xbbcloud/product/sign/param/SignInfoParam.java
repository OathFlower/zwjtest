package cn.xunhou.web.xbbcloud.product.sign.param;


import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author wangkm
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SignInfoParam {
    /**
     * 租户id
     */
    private Long tenantId;
    /**
     * 客户类型
     *
     * @see SignServerProto.EnumSignCustomerType
     */
    @NotNull(message = "客户类型不能为空")
    private Integer customerType;
    /**
     * 项目id
     */
    @NotEmpty(message = "项目id不能为空")
    private List<Long> projectIds;
    /**
     * 更新时间 YYYY-mm-dd hh:MM:ss
     */
    @NotBlank(message = "更新时间不能为空")
    private String useToDate;

    /**
     * 商务合同id
     */
    @NotNull(message = "商务合同id不能为空")
    private Long businessContractId;
}
