package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;

import java.math.RoundingMode;

public class XhNumberUtils {

    /**
     * 数字转换为千分位
     *
     * @param number   322425100.133331
     * @param decimals 小数位
     * @return 322, 425, 100.13
     */
    public static String millimeterFormat(Number number, Integer decimals) {
        StringBuilder pattern = new StringBuilder(",##");
        if (decimals > 0) {
            pattern.append("0.");
            for (int i = 0; i < decimals; i++) {
                pattern.append('0');
            }
        } else {
            pattern.append("#");
        }
        return NumberUtil.decimalFormat(pattern.toString(), number, RoundingMode.DOWN);
    }

    /**
     * 啥也不干，放到工具中方便找
     * 金额转中文大写
     *
     * @param number 金额
     * @return 中文大写
     */
    public static String digitToChinese(Number number) {
        return Convert.digitToChinese(number);
    }
}
