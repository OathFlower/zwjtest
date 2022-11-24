package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.xunhou.cloud.core.exception.SystemRuntimeException;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;


public class CommonUtil {


    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]*");
    /**
     * 英文+数字
     */
    private static final Pattern ENGLISH_NUMBERS = Pattern.compile("^[A-Za-z0-9]+$");

    public static boolean isEmployeeNum(String str) {
        return ENGLISH_NUMBERS.matcher(str).matches();
    }

    public static boolean isNumeric(String str) {
        return NUMERIC_PATTERN.matcher(str).matches();
    }


    public static Date getTomorrowWeeHours() {

        Calendar cal = Calendar.getInstance();
        cal.add(cal.DATE, 1);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return cal.getTime();
    }


    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @return
     */
    public static Boolean isEffectiveDate() {
        //当天凌晨12点
        Calendar cal = Calendar.getInstance();
        cal.add(cal.DATE, 0);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Date startTime = cal.getTime();

        //当天早上6点
        Calendar cal1 = Calendar.getInstance();
        cal1.add(cal1.DATE, 0);
        cal1.set(cal1.get(Calendar.YEAR), cal1.get(Calendar.MONTH), cal1.get(Calendar.DAY_OF_MONTH), 6, 0, 0);
        Date endTime = cal1.getTime();
        Date nowTime = new Date();
        if (nowTime.getTime() == startTime.getTime() || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @param timestamp 日期时间戳
     * @return 日期
     */
    public static Integer taxMonthCount(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp);
        DateTime now = DateTime.now();
        int y = DateUtil.year(now) - DateUtil.year(dateTime);
        if (y < 0) {
            throw new SystemRuntimeException("入职时间不能小于当前时间");
        } else if (y == 0) {
            Long t = DateUtil.betweenMonth(dateTime, now, true);
            return t.intValue() + 1;
        } else {
            return DateUtil.month(now) + 1;
        }
    }
}
