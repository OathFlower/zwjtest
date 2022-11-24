package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.StrUtil;
import com.google.protobuf.ByteString;

public class ProtoConvert {
    public static String string(String value) {
        return StrUtil.isBlank(value) ? "" : value;
    }

    public static Integer integer(Integer value) {
        return value == null ? 0 : value;
    }

    public static Boolean bool(Boolean value) {
        return value != null && value;
    }

    public static final String DEFAULT_STR = "";

    public static String nonnull(String p) {
        if (p == null) {
            return DEFAULT_STR;
        }
        return p;
    }

    public static int nonnull(Integer p) {
        if (p == null) {
            return 0;
        }
        return p;
    }

    public static long nonnull(Long p) {
        if (p == null) {
            return 0;
        }
        return p;
    }

    public static ByteString toByteString(String p) {
        if (p == null) {
            return ByteString.EMPTY;
        }
        return ByteString.copyFromUtf8(p);
    }

    public static String maynull(String p) {
        return StrUtil.trimToNull(p);
    }

    public static <T extends Number> T mynull(T p) {
        if (p == null) {
            return null;
        }
        int compare = CompareUtil.compare(0, p, true);
        if (compare < 0) {
            return p;
        }
        return null;
    }
}
