package cn.xunhou.web.xbbcloud.product.hrm.param;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.xunhou.cloud.core.context.XbbUserContext;
import cn.xunhou.grpc.proto.hrm.HrmServiceProto;
import cn.xunhou.web.xbbcloud.util.ProtoConvert;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sha.li
 * @since 2022/9/15
 */
@Getter
@Setter
@ToString
public class ImportEmployeeRowData implements Serializable {
    private static final long serialVersionUID = 2268278689336296502L;

    /**
     * 工号
     */
    @ExcelProperty("工号")
    private String employeeNum;
    /**
     * 姓名
     */
    @ExcelProperty("姓名")
    private String name;
    /**
     * 部门id
     */
    @ExcelIgnore
    private Long orgId;
    /**
     * 所属部门
     */
    @ExcelProperty("部门")
    private String orgName;
    /**
     * 手机号
     */
    @ExcelProperty("手机号码")
    private String mobile;
    /**
     * 用工类型id jobType
     */
    @ExcelIgnore
    private Long employmentTypeId;
    /**
     * 用工类型
     */
    @ExcelProperty("用工类型（工作性质）")
    private String employmentType;
    // /**
    //  * 用工来源id inviteType
    //  */
    // @ExcelIgnore
    // private Long employmentSourceId;
    // /**
    //  * 用工来源
    //  */
    // @ExcelProperty("用工来源（招聘渠道）")
    // private String employmentSource;
    /**
     * 证件类型
     */
    @ExcelIgnore
    private Integer idCardTypeCode;
    /**
     * 证件类型
     */
    @ExcelProperty("证件类型")
    private String idCardType;
    /**
     * 证件号码
     */
    @ExcelProperty("证件号码")
    private String idCardNum;
    /**
     * 入职日期
     */
    @ExcelIgnore
    private DateTime entryDate;
    /**
     * 入职日期
     */
    @ExcelProperty("入职日期")
    private String entryDateStr;
    /**
     * 薪资类型 payType
     */
    @ExcelIgnore
    private Integer salaryTypeId;
    /**
     * 薪资类型
     */
    @ExcelProperty("薪资类型")
    private String salaryType;
    /**
     * 员工归属 employeeSource
     */
    @ExcelIgnore
    private Integer employeeAttributionId;
    /**
     * 员工归属
     */
    @ExcelProperty("员工归属")
    private String employeeAttribution;

    public static List<HrmServiceProto.SaveOrUpdateEmployeeRequest> toRequestList(Collection<ImportEmployeeRowData> param) {
        if (param == null) {
            return Collections.emptyList();
        }
        return param.stream().map(ImportEmployeeRowData::toRequest).collect(Collectors.toList());
    }

    public static HrmServiceProto.SaveOrUpdateEmployeeRequest toRequest(ImportEmployeeRowData param) {
        if (param == null) {
            return null;
        }
        XbbUserContext xbbUserContext = XbbUserContext.newSingleInstance();
        return HrmServiceProto.SaveOrUpdateEmployeeRequest.newBuilder()
                .setName(ProtoConvert.nonnull(param.getName()))
                .setMobile(ProtoConvert.nonnull(param.getMobile()))
                .setIdCardType(ProtoConvert.nonnull(param.getIdCardTypeCode()))
                .setIdCard(ProtoConvert.nonnull(param.getIdCardNum()))
                .setPersonNumber(ProtoConvert.nonnull(param.getEmployeeNum()))
                .setEmployeeSource(ProtoConvert.nonnull(param.getEmployeeAttributionId()))
                .setJobType(ProtoConvert.nonnull(param.getEmploymentTypeId()))
                .setLastEntryDate(Opt.ofNullable(param.getEntryDate()).map(Date::getTime).orElse(0L))
                .setOrgId(ProtoConvert.nonnull(param.getOrgId()))
                .setPayType(ProtoConvert.nonnull(param.getSalaryTypeId()))
                // .setInviteType(ProtoConvert.nonnull(param.getEmploymentSourceId()))
                .setOperationId(xbbUserContext.get().getUserId())
                .setProductId(xbbUserContext.get().getProductId())
                .setSource(4)
                .setAccountType(2)
                .build();
    }


    public void setEmployeeNum(String employeeNum) {
        this.employeeNum = trimAllSpace(employeeNum);
    }

    public void setName(String name) {
        this.name = trimAllSpace(name);
    }

    public void setOrgName(String orgName) {
        this.orgName = trimAllSpace(orgName);
    }

    public void setMobile(String mobile) {
        this.mobile = trimAllSpace(mobile);
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = trimAllSpace(employmentType);
    }

    // public void setEmploymentSource(String employmentSource) {
    //     this.employmentSource = trimAllSpace(employmentSource);
    // }

    public void setIdCardType(String idCardType) {
        this.idCardType = trimAllSpace(idCardType);
    }

    public void setIdCardNum(String idCardNum) {
        this.idCardNum = trimAllSpace(idCardNum);
    }

    public void setEntryDateStr(String entryDateStr) {
        this.entryDateStr = trimAllSpace(entryDateStr);
    }

    public void setSalaryType(String salaryType) {
        this.salaryType = trimAllSpace(salaryType);
    }

    public void setEmployeeAttribution(String employeeAttribution) {
        this.employeeAttribution = trimAllSpace(employeeAttribution);
    }

    public String trimAllSpace(String str){
        return StrUtil.emptyToNull(StrUtil.replace(str, " ", ""));
    }
}
