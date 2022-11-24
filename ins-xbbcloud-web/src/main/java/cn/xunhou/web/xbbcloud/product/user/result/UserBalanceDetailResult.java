package cn.xunhou.web.xbbcloud.product.user.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@ToString
public class UserBalanceDetailResult {

    /**发薪日期*/
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payDate;
    /**租户*/
    private String tenantName;
    /**发薪账户*/
    private String subjectName;
    /**金额*/
    private BigDecimal money;
    /**发薪时间*/
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalDateTime payTime;
}
