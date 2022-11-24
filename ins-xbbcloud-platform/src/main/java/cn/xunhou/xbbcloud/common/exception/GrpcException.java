package cn.xunhou.xbbcloud.common.exception;

import cn.xunhou.cloud.core.util.XbbBusinessStatusUtil;
import io.grpc.Status;
import org.springframework.lang.NonNull;

/**
 * @author litb
 * @date 2022/7/28 13:45
 * <p>
 * 封装grpc协议异常
 */
public class GrpcException {

    public static RuntimeException runtimeException(@NonNull Status status, @NonNull String errMsg) {
        return status.withDescription(errMsg).asRuntimeException();
    }

    public static RuntimeException asRuntimeException(@NonNull String errMsg) {
        return Status.INTERNAL.withDescription(errMsg).asRuntimeException();
    }

    public static RuntimeException asRuntimeException(@NonNull Throwable throwable) {
        return Status.INTERNAL.withDescription(throwable.getMessage()).asRuntimeException();
    }

    public static RuntimeException runtimeException(@NonNull XbbCloudErrorCode xbbCloudErrorCode) {
        return XbbBusinessStatusUtil.asRuntimeException(xbbCloudErrorCode.getCode(),
                xbbCloudErrorCode.getMessage());
        //return Status.INTERNAL.withDescription(xbbCloudErrorCode.getMessage()).asRuntimeException();
    }
}
