package cn.xunhou.xbbcloud.common.exception;

import cn.xunhou.cloud.core.util.XbbBusinessStatusUtil;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

/**
 * @author litb
 * @date 2022/9/7 14:25
 * <p>
 * grpc自定义异常处理器,通过该异常处理器将业务异常包装后返回调用方
 */
@GrpcAdvice
public class XbbCloudGrpcExceptionHandler {


    @GrpcExceptionHandler(XbbCloudException.class)
    public StatusRuntimeException handleAssetXhException(XbbCloudException xbbCloudException) {
        return XbbBusinessStatusUtil.asRuntimeException(xbbCloudException.getErrorCode(),
                xbbCloudException.getErrorMessage());
    }
}
