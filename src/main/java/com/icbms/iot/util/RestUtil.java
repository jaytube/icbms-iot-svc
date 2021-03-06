package com.icbms.iot.util;

import com.alibaba.fastjson.JSON;
import com.icbms.iot.common.CommonResponse;
import com.icbms.iot.exception.IotException;
import com.icbms.iot.rest.LoRaCommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.List;
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

    public static final String HTTP_HEADER_CONTENT_TYPE = "application/json;charset=UTF-8";

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * JWT TOKEN值
     */
    private static final String _jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb3JhLWFwcC1zZXJ2ZXIiLCJhdWQiOiJ" +
            "sb3JhLWFwcC1zZXJ2ZXIiLCJuYmYiOjE1Mzc0MDgzMDMsImV4cCI6MzMwOTQzMTcxMDMsInN1YiI6I" +
            "nVzZXIiLCJ1c2VybmFtZSI6ImFkbWluIn0.14eVliflc5oG5FJXIphEfcWbc5A4DxzTk-u5AMaIsJc";

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doGet(String url) {
        log.debug("【doGet】【请求URL】：{}", url);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doGet】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doPost(String url, Map<String, Object> params) {
        log.debug("【doPost】【请求URL】：{}", url);
        log.debug("【doPost】【请求入参】：{}", params);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("grpc-metadata-authorization", _jwt_token);
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doPost】【请求响应】：{}", response);
        return response(url, params, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doGetNoToken(String url) {
        log.debug("【doGetNoToken】【请求URL】：{}", url);
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        HttpStatus statusCode = exchange.getStatusCode();
        Map response = exchange.getBody();
        log.debug("【doGetNoToken】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doPostFormDataNoToken(String url, Map<String, Object> paramsMap) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
            params.add(entry.getKey(), entry.getValue().toString());
        }
        log.debug("【doPostFormDataNoToken】【请求URL】：{}", url);
        log.debug("【doPostFormDataNoToken】【请求入参】：{}", params);
        RestTemplate restTemplate = new RestTemplate();
        //设置请求头(注意会产生中文乱码)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        //发送请求，设置请求返回数据格式为String
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, request, Map.class);
        Map response = responseEntity.getBody();
        log.debug("【doPostFormDataNoToken】【请求响应】：{}", response);
        return response(url, paramsMap, responseEntity);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 1,
            backoff = @Backoff(delay = 500L, multiplier = 1))
    public CommonResponse<Map> doGetWithToken(String gatewayIp, String url) {
        log.debug("【doGetWithToken】【请求URL】：{}", url);
        HttpHeaders requestHeaders = createHeader(gatewayIp);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doGetWithToken】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Recover
    public CommonResponse<Map> recover(RestClientException e) {
        return CommonResponse.faild(e.getMessage(), null);
    }

    @Recover
    public CommonResponse<Map> recover(IotException e) {
        log.error("exception when call remote service, {}", e);
        return CommonResponse.faild(e.getErrorCode()+ ", " + e.getErrorMessage(), null);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doGetWithToken(String url, HttpHeaders requestHeaders) {
        log.debug("【doGetWithToken】【请求URL】：{}", url);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doGetWithToken】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doPostWithToken(String gatewayIp, String url, Map<String, Object> params) {
        log.debug("【doPostWithToken】【请求URL】：{}", url);
        log.debug("【doPostWithToken】【请求入参】：{}", params);
        HttpHeaders requestHeaders = createHeader(gatewayIp);
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doPostWithToken】【请求响应】：{}", response);
        return response(url, params, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doDeleteWithToken(String gatewayIp, String url) {
        log.debug("【doDeleteWithToken】【请求URL】：{}", url);
        HttpHeaders requestHeaders = createHeader(gatewayIp);
        HttpEntity<Map> requestEntity = new HttpEntity<>(null, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doDeleteWithToken】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doPlainPost(String url,  Map<String, String> params) {
        log.debug("【Plain Post】【请求URL】：{}", url);
        HttpHeaders requestHeaders = new HttpHeaders();
        HttpEntity<Map> requestEntity = new HttpEntity<>(params, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doPost】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    @Retryable(value = RestClientException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 500L, multiplier = 2))
    public CommonResponse<Map> doDeleteWithToken(String gatewayIp, String url, List<Map> body) {
        log.debug("【doDeleteWithToken】【请求URL】：{}", url);
        HttpHeaders requestHeaders = createHeader(gatewayIp);
        HttpEntity<List> requestEntity = new HttpEntity<>(body, requestHeaders);
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Map.class);
        Map response = exchange.getBody();
        log.debug("【doDeleteWithToken】【请求响应】：{}", response);
        return response(url, null, exchange);
    }

    private CommonResponse<Map> response(String url, Map<String, Object> params, ResponseEntity<Map> exchange) {
        if (exchange.getStatusCode() == HttpStatus.OK) {
            return CommonResponse.success(exchange.getBody());
        } else {
            String message;
            if (MapUtils.isEmpty(params)) {
                message = "[URL]:" + url;
            } else {
                String paramsStr = JSON.toJSONString(params);
                message = "[URL]:" + url + "[PARAMS]:" + paramsStr;
            }
            logger.info("POST FAILED.");
            return new CommonResponse(exchange.getStatusCodeValue(), message, exchange.getBody());
        }
    }

    private HttpHeaders createHeader(String gatewayIp) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("Content-Type", HTTP_HEADER_CONTENT_TYPE);
        requestHeaders.add("Authorization", loRaCommandService.getRedisToken(gatewayIp));
        requestHeaders.add("Tenant", loRaCommandService.getDbInstanceFromRedis(gatewayIp, "cluing"));
        return requestHeaders;
    }

}
