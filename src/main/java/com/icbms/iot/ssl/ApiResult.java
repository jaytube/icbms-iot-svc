package com.icbms.iot.ssl;

import lombok.Data;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: ApiResult
 */
@Data
public class ApiResult {

    public int code;
    public String message;
    public boolean success;
    public Object data;
}
