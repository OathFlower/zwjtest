package cn.xunhou.web.xbbcloud.product.hrm.param;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;


@Getter
@Setter
@ToString
public class ImportSalaryRowData implements Serializable {
    private static final long serialVersionUID = 2268278689336296502L;


    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;


    @ExcelProperty("身份证号")
    private String idCardNo;

    @ExcelProperty("个税金额")
    private String taxAmount;
    @ExcelIgnore
    private BigDecimal taxAmountBig;

    @ExcelProperty("薪资发放金额（应发金额，包含个税）")
    private String paidAbleAmount;
    @ExcelIgnore
    private BigDecimal paidAbleAmountBig;
    @ExcelProperty("备注")
    private String remark;

    public void setName(String name) {
        this.name = StrUtil.trimToNull(name);
    }

    public void setPhone(String phone) {
        this.phone = StrUtil.trimToNull(phone);
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = StrUtil.trimToNull(idCardNo);
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = StrUtil.trimToNull(taxAmount);
    }


    public void setPaidInAmount(String paidAbleAmount) {
        this.paidAbleAmount = StrUtil.trimToNull(paidAbleAmount);
    }

    public void setRemark(String remark) {
        this.remark = StrUtil.trimToNull(remark);
    }
}
