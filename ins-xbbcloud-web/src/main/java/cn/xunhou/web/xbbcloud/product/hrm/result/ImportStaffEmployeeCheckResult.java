package cn.xunhou.web.xbbcloud.product.hrm.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 导入人力员工检查结果
 *
 * @author TangYitong
 * @date 2022/10/25
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class ImportStaffEmployeeCheckResult {

    /**
     * 匹配数量
     */
    private Integer matchNum;

    /**
     * 员工信息
     */
    private List<EmployeeInfo> list;

    /**
     * 缓存使用
     */
    private String key;

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class EmployeeInfo {
        /**
         * 勋厚员工信息
         */
        private StaffResult staff;
        /**
         * 员工姓名
         */
        private String employeeName;
        /**
         * 员工手机号
         */
        private String employeeTel;
        /**
         * 员工id
         */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long employeeId;
        /**
         * 员工证件类型
         */
        private Integer employeeIdCardTypeCode;
        /**
         * 员工证件号码
         */
        private String employeeIdCardNum;
        /**
         * 员工组织
         */
        private Long employeeOrgId;
    }

}
