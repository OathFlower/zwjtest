package cn.xunhou.web.xbbcloud.product.sign.result;

import cn.xunhou.grpc.proto.xbbcloud.SignServerProto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class SignInfoResult {
    /**
     * 是否启用 1启用 2不启用
     */
    private Integer isUse;
    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 客户类型
     *
     * @see SignServerProto.EnumSignCustomerType
     */
    private Integer customerType;
    /**
     * 项目id
     */
    private List<Long> projectIds;
    /**
     * 更新时间 YYYY-mm-dd hh:MM:ss
     */
    private String updateTime;

    /**
     * 到期时间 yyyy-MM-dd hh:mm:ss
     */
    private String useToDate;

    /**
     * 商务合同
     */
    private Long businessContractId;

}
