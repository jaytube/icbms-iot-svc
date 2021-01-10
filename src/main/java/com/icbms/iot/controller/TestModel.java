package com.icbms.iot.controller;

import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: TestModel
 */
public class TestModel {

    private String url;
    private Map<String, String> json;
    private String code;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getJson() {
        return json;
    }

    public void setJson(Map<String, String> json) {
        this.json = json;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
