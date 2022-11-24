package cn.xunhou.xbbcloud.rpc.sign.entity;


import cn.xunhou.cloud.dao.annotation.XbbTable;
import cn.xunhou.cloud.dao.xhjdbc.XbbSnowTimeEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@XbbTable(table = "sign_info")
public class SignInfoEntity extends XbbSnowTimeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 到期时间
     */
    private Timestamp useToDate;

    /**
     * 客户类型: 1-内部
     */
    private Integer customerType;

    /**
     * 商务合同id
     */
    private Long businessContractId;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 操作人
     */
    private Long operatorId;

    /**
     * 扩展信息
     */
    private String extInfo;

    /**
     * 请注意默认值
     */
    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class ExtInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 商业客户id
         */
        private Long businessContractCustomerId;
    }
}
