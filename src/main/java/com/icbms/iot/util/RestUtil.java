package com.icbms.iot.util;

import com.icbms.iot.rest.LoRaCommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: RestUtil
 */
@Component
@Slf4j
public class RestUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoRaCommandService loRaCommandService;

    /**
     * JWT TOKEN值
     */
    private static final String _jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb3JhLWFwcC1zZXJ2ZXIiLCJhdWQiOiJ" +
            "sb3JhLWFwcC1zZXJ2ZXIiLCJuYmYiOjE1Mzc0MDgzMDMsImV4cCI6MzMwOTQzMTcxMDMsInN1YiI6I" +
            "nVzZXIiLCJ1c2VybmFtZSI6ImFkbWluIn0.14eVliflc5oG5FJXIphEfcWbc5A4DxzTk-u5AMaIsJc";

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doGet(String url) {
        log.info("【doGet】【请求URL】：{}", url);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.info("【doGet】【请求响应】：{}", response);
        return response;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doPost(String url, Map<String, Object> params) {
        log.info("【doPost】【请求URL】：{}", url);
        log.info("【doPost】【请求入参】：{}", params);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.info("【doPost】【请求响应】：{}", response);
        return response;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doPostFormDataNoToken(String url, MultiValueMap<String, String> params) {
        log.info("【doPostFormDataNoToken】【请求URL】：{}", url);
        log.info("【doPostFormDataNoToken】【请求入参】：{}", params);
        RestTemplate restTemplate = new RestTemplate();
        //设置请求头(注意会产生中文乱码)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        //发送请求，设置请求返回数据格式为String
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);
        Map response = responseEntity.getBody();
        log.info("【doPostFormDataNoToken】【请求响应】：{}", response);
        return response;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doGetWithToken(String url, Map<String, Object> params) {
        log.info("【doGetWithToken】【请求URL】：{}", url);
        log.info("【doGetWithToken】【请求入参】：{}", params);
        HttpHeaders requestHeaders = createHeader();
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange;
        if (MapUtils.isEmpty(params)) {
            exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        } else {
            exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class, params);
        }
        Map response = exchange.getBody();
        log.info("【doGetWithToken】【请求响应】：{}", response);
        return response;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doPostWithToken(String url, Map<String, Object> params) {
        log.info("【doPostWithToken】【请求URL】：{}", url);
        log.info("【doPostWithToken】【请求入参】：{}", params);
        HttpHeaders requestHeaders = createHeader();
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.info("【doPostWithToken】【请求响应】：{}", response);
        return response;
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 5000L, multiplier = 2))
    public Map doDeleteWithToken(String url) {
        log.info("【doDeleteWithToken】【请求URL】：{}", url);
        HttpHeaders requestHeaders = createHeader();
        HttpEntity<Map> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.info("【doDeleteWithToken】【请求响应】：{}", response);
        return response;
    }

    private HttpHeaders createHeader() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", "application/json;charset=UTF-8");
        requestHeaders.add("Authorization", loRaCommandService.getRedisToken());
        requestHeaders.add("Tenant", "20190701_cluing");
        return requestHeaders;
    }

}
