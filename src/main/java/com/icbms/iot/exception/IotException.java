package com.icbms.iot.exception;

public class IotException extends RuntimeException {

    int errorCode;
    String errorMessage;
    Throwable t;

    public IotException(ErrorCodeEnum errorCodeEnum) {
        this(errorCodeEnum.getCode(), errorCodeEnum.getMsg());
    }

    public IotException(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public IotException(int errorCode, String errorMessage, Throwable t) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.t = t;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Throwable getT() {
        return t;
    }

    public void setT(Throwable t) {
        this.t = t;
    }
}
