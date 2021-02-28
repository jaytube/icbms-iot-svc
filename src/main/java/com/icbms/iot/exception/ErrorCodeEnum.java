package com.icbms.iot.exception;

public enum ErrorCodeEnum {

    IOT_MESSAGE_HEAD_INCORRECT(1001, "payload head is not correct"),
    IOT_MESSAGE_END_INCORRECT(1002, "payload end is not correct"),
    IOT_MESSAGE_NULL(1003, "realtime message is null"),
    TOKEN_FETCH_ERROR(1004, "Get Token Error"),
    REMOTE_CALL_FAILED(1005, "remote call failed.");

    private int code;
    private String msg;

    ErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
