package cn.xunhou.web.xbbcloud.product.manage.param;

import cn.xunhou.web.xbbcloud.config.xhrpc.enums.EnumXhTenant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangkm
 */
@Setter
@Getter
@ToString
@Accessors(chain = true)
public class CustomerContractParam {
    private QueryDto queryDto;
    private EnumXhTenant tenant;

    @Setter
    @Getter
    @ToString
    @Accessors(chain = true)
    public static class QueryDto {
        /**
         * 合同名称
         */
        private String contractName;

        /**
         * 客户ID
         */
        private List<Long> customerIds;

        /**
         * 客户ID
         */
        private Long customerId;

        /**
         * 状态
         */
        private List<String> statuses;
        /**
         * 合同签署主体
         */
        private Long subjectId;

        /**
         * 合同是否已回
         */
        private String returnFlag;

        /**
         * 销售账号ID
         */
        private Long saleAccountId;

        /**
         * 是否归属猎聘
         */
        private String belongLiepinFlag;

        /**
         * 商机来源Id
         */
        private Long businessSourceId;

        /**
         * 业务类型
         */
        private String businessType;

        /**
         * 合同创建开始时间 YYYYMMDD
         */
        private String createStartTime;

        /**
         * 合同创建结束时间 YYYYMMDD
         */
        private String createEndTime;

        /**
         * 权限字段
         */
        private List<Long> customerIdsWithAuth;

        private List<Long> customerContractIdsWithAuth;

        /**
         * 页码
         */
        private Integer curPage = 0;

        /**
         * 分页大小
         */
        private Integer pageSize = 15;

    }
}
