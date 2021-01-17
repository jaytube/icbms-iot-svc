package com.icbms.iot.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: RestUtil
 */
@Component
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * JWT TOKEN值
     */
    private static final String _jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb3JhLWFwcC1zZXJ2ZXIiLCJhdWQiOiJ" +
            "sb3JhLWFwcC1zZXJ2ZXIiLCJuYmYiOjE1Mzc0MDgzMDMsImV4cCI6MzMwOTQzMTcxMDMsInN1YiI6I" +
            "nVzZXIiLCJ1c2VybmFtZSI6ImFkbWluIn0.14eVliflc5oG5FJXIphEfcWbc5A4DxzTk-u5AMaIsJc";

    public Map doGet(String url) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        return exchange.getBody();
    }

    public Map doPost(String url, Map<String, Object> params) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        return exchange.getBody();
    }

}
