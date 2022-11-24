package cn.xunhou.xbbcloud.common.utils;

import cn.xunhou.xbbcloud.common.exception.GrpcException;
import io.grpc.Status;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author litb
 * @date 2022/8/1 20:16
 */
public class IAssert extends cn.xunhou.common.tools.util.IAssert {

    public static void notNull(Object o, String msg) {
        if (o == null) {
            throw GrpcException.asRuntimeException(msg);
        }
    }

    public static void notBank(String o, String msg) {
        if (StringUtils.isBlank(o)) {
            throw GrpcException.asRuntimeException(msg);
        }
    }

    public static void notEmpty(Collection<?> collection, String msg) {
        if (CollectionUtils.isEmpty(collection)) {
            throw GrpcException.asRuntimeException(msg);
        }
    }

    public static void state(String msg, boolean... condition) {
        for (boolean b : condition) {
            state(b, msg);
        }
    }

    /**
     * 如果多个条件中至少有一个为true,则合法 否则异常
     *
     * @param msg       异常消息
     * @param condition 条件
     */
    public static void hasTrue(String msg, boolean... condition) {
        boolean result = false;
        for (boolean b : condition) {
            result = result | b;
        }
        if (!result) {
            throw GrpcException.asRuntimeException(msg);
        }
    }

    /**
     * @param condition false 抛出异常
     * @param msg       异常消息
     */
    public static void state(boolean condition, String msg) {
        if (!condition) {
            throw GrpcException.asRuntimeException(msg);
        }
    }

    /**
     * @param condition false 抛出异常
     * @param msg       异常消息
     * @param status    状态
     */
    public static void state(boolean condition, String msg, Status status) {
        if (!condition) {
            throw GrpcException.runtimeException(status, msg);
        }
    }

    public static <E extends Throwable> void state(boolean condition, Supplier<E> throwableSupplier) throws E {
        if (!condition) {
            throw throwableSupplier.get();
        }
    }
}
