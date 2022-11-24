package cn.xunhou.xbbcloud.common.utils;

import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiang.tian
 * @since 2022-6-30
 */
public class ConvertUtil {
    /**
     * 布尔转int
     */
    public static int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    public static Long int64ToLong(long l) {
        if (l == 0) {
            return null;
        }
        return l;
    }

    public static Integer int32ToInteger(int i) {
        if (i == 0) {
            return null;
        }
        return i;
    }

    public static String stringNoBlank(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        return str;
    }

    public static Long convertByLong(Long l) {
        if (Objects.isNull(l)) {
            return 0L;
        }
        return l;
    }

    public static Integer convertByInteger(Integer i) {
        if (Objects.isNull(i)) {
            return 0;
        }
        return i;
    }


    /**
     * int64转时间戳
     */
    public static Timestamp int64ToTimestamp(long l) {
        if (l <= 0) {
            return null;
        }
        return new Timestamp(l);
    }

    public static long longToInt64(Long l) {
        return l == null ? 0 : l;
    }

    public static long timestampToInt64(Timestamp t) {
        if (t == null) {
            return 0;
        }
        return t.getTime();
    }

    public static boolean isIndistinct(String value) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(value);
        return m.find();
    }
}
