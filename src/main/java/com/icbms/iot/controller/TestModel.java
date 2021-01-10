package com.icbms.iot.controller;

import lombok.Data;

import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: TestModel
 */
@Data
public class TestModel {

    private String url;
    private Map<String, Object> json;
    private String code;

}
