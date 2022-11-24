package cn.xunhou.xbbcloud.common.exception;

/**
 * @author litb
 * @date 2022/7/28 11:05
 * <p>
 * xbbcloud平台异常
 */
public class XbbCloudException extends RuntimeException {

    private static final long serialVersionUID = -1352783164976801595L;

    private Integer errorCode;

    private String errorMessage;

    public XbbCloudException() {
        super();
    }

    public XbbCloudException(String msg) {
        super(msg);
        this.errorMessage = msg;
        this.errorCode = XbbCloudErrorCode.SYSTEM_ERROR.getCode();
    }

    public XbbCloudException(XbbCloudErrorCode xbbCloudErrorCode) {
        super(xbbCloudErrorCode.getMessage());
        this.errorCode = xbbCloudErrorCode.getCode();
        this.errorMessage = xbbCloudErrorCode.getMessage();
    }

    public XbbCloudException(XbbCloudErrorCode xbbCloudErrorCode, String message) {
        super(message);
        this.errorCode = xbbCloudErrorCode.getCode();
        this.errorMessage = message;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
