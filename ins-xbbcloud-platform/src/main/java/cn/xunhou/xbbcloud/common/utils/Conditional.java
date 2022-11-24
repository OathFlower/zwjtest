package cn.xunhou.xbbcloud.common.utils;

import java.util.function.Supplier;

/**
 * @author litb
 * @date 2022/9/13 20:33
 * <p>
 * 条件执行工具类
 */
public class Conditional {

    public static void run(boolean condition, Runnable runnable) {
        if (condition) {
            runnable.run();
        }
    }

    public static <T> T run(boolean condition, Supplier<T> supplier, T defaultValue) {
        if (condition) {
            return supplier.get();
        }
        return defaultValue;
    }
}
