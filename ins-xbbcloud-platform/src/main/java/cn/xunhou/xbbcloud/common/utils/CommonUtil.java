package cn.xunhou.xbbcloud.common.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;

import java.sql.Timestamp;


public class CommonUtil {
    /**
     * @param timestamp 日期时间戳
     * @return 日期
     */
    public static Integer taxMonthCount(Timestamp timestamp) {
        DateTime dateTime = new DateTime(timestamp);
        DateTime now = DateTime.now();
        int y = DateUtil.year(now) - DateUtil.year(dateTime);
        if (y < 0) {
            throw ExceptionUtil.wrapRuntime("入职时间不能小于当前时间");
        } else if (y == 0) {
            Long t = DateUtil.betweenMonth(dateTime, now, true);
            return t.intValue() + 1;
        } else {
            return DateUtil.month(now) + 1;
        }
    }
}
