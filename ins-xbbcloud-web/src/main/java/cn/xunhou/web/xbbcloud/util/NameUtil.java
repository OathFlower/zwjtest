package cn.xunhou.web.xbbcloud.util;

import cn.hutool.core.lang.Validator;

import java.util.regex.Pattern;

/**
 * @author sha.li
 * @since 2022/9/15
 */
public class NameUtil {
    public static final Pattern ENGLISH_NAME_PATTERN = Pattern.compile("^[A-Za-z//-·]{2,60}$");
    /**
     * 中文 ，字母 、数字、横杠符号
     */
    public static final Pattern EMPLOYEE_NAME_PATTERN = Pattern.compile("^[\u2E80-\u9FFFA-Za-z0-9\\-·]{2,60}$");

    private NameUtil() {
    }

    /**
     * 是否为员工姓名
     */
    public static boolean isEmployeeName(String name) {
        return EMPLOYEE_NAME_PATTERN.matcher(name).matches();
    }

    /**
     * 是否为中文名或英文名
     */
    public static boolean isName(String name) {
        return isChineseName(name) || isEnglishName(name);
    }

    /**
     * 是否为中文名
     */
    public static boolean isChineseName(String name) {
        return Validator.isChineseName(name);
    }

    /**
     * 是否为中文英文名
     */
    public static boolean isEnglishName(String name) {
        return ENGLISH_NAME_PATTERN.matcher(name).matches();
    }
}
