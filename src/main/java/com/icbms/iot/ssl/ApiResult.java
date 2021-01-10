package com.icbms.iot.ssl;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: ApiResult
 */
public class ApiResult {

    public int code;
    public String message;
    public boolean success;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
