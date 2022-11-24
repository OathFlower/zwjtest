package cn.xunhou.xbbcloud.common.utils;

import cn.xunhou.xbbcloud.common.exception.GrpcException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 个税相关工具类
 * https://wenku.baidu.com/view/3b6213c4132de2bd960590c69ec3d5bbfd0ada2e.html
 */
@Slf4j
public class TaxUtil {
    public static void main(String[] args) {
        System.out.println(IIT(16000000,8000000,3,0,0,498000));
    }

    /**
     * 应税工资基数 5000块钱
     */
    public static final BigDecimal TAXABLE_WAGE_BASE = new BigDecimal(5000);


    /**
     * 个人所得税计算 单位（分）
     * @param _totalIncome 累计收入总数
     * @param _curIncome 当前收入
     * @param freeOfDutyMonth 免税收入月数
     * @param _totalAccumulationFund 累计公积金总数
     * @param _totalSocialInsurance 累计社保总数
     * @param _historyTotalTax 历史个税累计总数
     */
    public static Integer IIT(@NonNull Integer _totalIncome
            ,@NonNull Integer _curIncome
            ,@NonNull Integer freeOfDutyMonth
            ,@NonNull Integer _totalAccumulationFund
            ,@NonNull Integer _totalSocialInsurance
            ,@NonNull Integer _historyTotalTax){
        log.info("IIT --- ,totalIncome = {},curIncome = {},freeOfDutyMonth = {},totalAccumulationFund = {},totalSocialInsurance = {},historyTotalTax = {}"
                ,_totalIncome,_curIncome,freeOfDutyMonth,_totalAccumulationFund,_totalSocialInsurance,_historyTotalTax);
        BigDecimal totalIncome = new BigDecimal(_totalIncome) .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
        BigDecimal curIncome = new BigDecimal(_curIncome) .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
        BigDecimal totalAccumulationFund = new BigDecimal(_totalAccumulationFund) .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
        BigDecimal totalSocialInsurance = new BigDecimal(_totalSocialInsurance) .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
        BigDecimal historyTotalTax = new BigDecimal(_historyTotalTax) .divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);

        if(curIncome.compareTo(TAXABLE_WAGE_BASE) <= 0){
            //用户当月发放金额“小于等于5000”，不扣个税
            return 0;
        }
        BigDecimal minusAmount = totalIncome.subtract(TAXABLE_WAGE_BASE.multiply(new BigDecimal(freeOfDutyMonth))).subtract(totalAccumulationFund).subtract(totalSocialInsurance);
        IITRateBean iitRate = getIITRate(totalIncome);
        int i = minusAmount.multiply(iitRate.getTaxRate()).subtract(iitRate.getQuickCalculationDeduction()).subtract(historyTotalTax).setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        log.info("IIT --- result = " + i);
        return Math.max(i, 0);
    }

    /**
     * 获取应纳所得税率
     */
    private static IITRateBean getIITRate(BigDecimal minusAmount){
        double v = minusAmount.doubleValue();
        BigDecimal taxRate;
        BigDecimal quickCalculationDeduction = BigDecimal.ZERO;
        if(v <= 36000){
            taxRate = new BigDecimal("0.03");
        }else if(v >36000  && v<=144000){
            taxRate = new BigDecimal("0.1");
            quickCalculationDeduction = new BigDecimal(2520);
        }else if(v > 144000 && v <= 3000000){
            taxRate = new BigDecimal("0.2");
            quickCalculationDeduction = new BigDecimal(16920);
        }else if(v > 3000000 && v <= 420000){
            taxRate = new BigDecimal("0.25");
            quickCalculationDeduction = new BigDecimal(31920);
        }else if(v > 420000 && v <= 660000){
            taxRate = new BigDecimal("0.3");
            quickCalculationDeduction = new BigDecimal(52920);
        }else if(v > 660000 && v <= 960000){
            taxRate = new BigDecimal("0.35");
            quickCalculationDeduction = new BigDecimal(85920);
        }else{
            taxRate = new BigDecimal("0.45");
            quickCalculationDeduction = new BigDecimal(181920);
        }
        return new IITRateBean().setTaxRate(taxRate).setQuickCalculationDeduction(quickCalculationDeduction);
    }

    @Getter
    @Setter
    @ToString
    @Accessors(chain = true)
    public static class IITRateBean{
        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 速算扣除数
         */
        private BigDecimal quickCalculationDeduction;
    }
}
