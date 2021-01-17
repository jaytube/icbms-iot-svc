package com.icbms.iot.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.icbms.iot.common.CommonResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/17
 * @Desc: HttpUtil
 */
public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private RestTemplate restTemplate;

    /**
     * JWT TOKEN值
     */
    private static final String _jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb3JhLWFwcC1zZXJ2ZXIiLCJhdWQiOiJ" +
            "sb3JhLWFwcC1zZXJ2ZXIiLCJuYmYiOjE1Mzc0MDgzMDMsImV4cCI6MzMwOTQzMTcxMDMsInN1YiI6I" +
            "nVzZXIiLCJ1c2VybmFtZSI6ImFkbWluIn0.14eVliflc5oG5FJXIphEfcWbc5A4DxzTk-u5AMaIsJc";

    /**
     * 发送 POST 请求
     *
     * @param url    API接口URL
     * @param params 参数map
     * @return
     */
    public static CommonResponse doPost(String url, Map<String, Object> params) {
        try {
            return doPost(url, convertObjectToJson(params), _jwt_token);
        } catch (IOException e) {
            logger.error("doPost error", e);
            return CommonResponse.faild(e.getMessage());
        }
    }

    public static CommonResponse doPost(String url, Map<String, Object> params, String jwtToken) {
        try {
            return doPost(url, convertObjectToJson(params), _jwt_token);
        } catch (IOException e) {
            logger.error("doPost error", e);
            return CommonResponse.faild(e.getMessage());
        }
    }

    public static CommonResponse doPost(String apiUrl, String param) {
        return doPost(apiUrl, param, _jwt_token);
    }

    public static CommonResponse doGet(String url) {
        return doGet(url, _jwt_token);
    }

    public static CommonResponse doGet(String url, String jwtToken) {
        CloseableHttpClient httpclient = createHttpClient();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        String out = null;
        JSONObject jsonObject = new JSONObject();//接收结果
        CommonResponse<JSONObject> responseData = new CommonResponse<>();
        try {
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("grpc-metadata-authorization", jwtToken);
            logger.info("httppost: " + JSON.toJSONString(httpGet));
            response = httpclient.execute(httpGet);
            //logger.info("response: " + JSON.toJSONString(response));
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("status code: " + statusCode);
            logger.info("entity: " + JSON.toJSONString(response.getEntity()));
            if (statusCode != HttpStatus.SC_OK) {
                out = EntityUtils.toString(response.getEntity(), "utf-8");
                logger.info(out + ",url: " + url); //打印错误信息
            } else {
                out = EntityUtils.toString(response.getEntity(), "utf-8");
            }
            jsonObject = JSONObject.parseObject(out);
            logger.info("response: " + JSON.toJSONString(jsonObject));
            responseData.setCode(200);
            responseData.setData(jsonObject);
        } catch (Exception e) {
            logger.error(e.getMessage() + ",url: " + url, e);
            jsonObject.put("message", e.getMessage());
            responseData.setCode(500);
            responseData.setData(jsonObject);
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
        }
        return responseData;
    }

    private static CommonResponse doPost(String url, String params, String jwtToken) {
        CloseableHttpClient httpclient = createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        String out = null;
        JSONObject jsonObject = new JSONObject();//接收结果
        CommonResponse<JSONObject> responseData = new CommonResponse<>();
        try {
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("grpc-metadata-authorization", jwtToken);
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            logger.info("httppost: " + JSON.toJSONString(httpPost));
            response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("status code: " + statusCode);
            logger.info("entity: " + JSON.toJSONString(response.getEntity()));
            if (statusCode != HttpStatus.SC_OK) {
                out = EntityUtils.toString(response.getEntity(), "utf-8");
                logger.info(out + ",url: " + url + " params: " + params); //打印错误信息
            } else {
                out = EntityUtils.toString(response.getEntity(), "utf-8");
            }
            jsonObject = JSONObject.parseObject(out);
            logger.info("response: " + JSON.toJSONString(jsonObject));
            responseData.setCode(200);
            responseData.setData(jsonObject);
        } catch (Exception e) {
            logger.error(e.getMessage() + ",url: " + url, e);
            jsonObject.put("message", e.getMessage());
            responseData.setCode(500);
            responseData.setData(jsonObject);
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return responseData;
    }

    public static String convertObjectToJson(Object data) throws IOException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(data);
        return json;
    }

    private static CloseableHttpClient createHttpClient() {
        return HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
    }

    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 600000;

    static {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", createSSLConnSocketFactory())
                .build();
        // 设置连接池
        connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置连接池大小
        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        // 在提交请求之前 测试连接是否可用
        configBuilder.setStaleConnectionCheckEnabled(true);
        requestConfig = configBuilder.build();
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            sslsf = new SSLConnectionSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }
}
