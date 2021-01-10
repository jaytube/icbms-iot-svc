package com.icbms.iot.ssl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.icbms.iot.util.CommonUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @Author: Cherry
 * @Date: 2021/1/10
 * @Desc: SSLConnectionSocketUtil
 */
public class SSLConnectionSocketUtil {

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
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            sslsf = new SSLConnectionSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
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
     * @param url      API请求链接
     * @param text     发送内容
     * @param deviceId 终端编码
     * @param port     发送端口
     */
    public static ApiResult sendMessage(String url, String text, String deviceId, int port) throws Exception {
        ApiResult apiResult = new ApiResult();
        try {
            String action = "nodes/" + deviceId + "/queue";
            byte[] commondBytes = CommonUtil.hexStringToBytes(text);

            DownLink link = new DownLink();
            link.setConfirmed(false);
            link.setData(CommonUtil.bytesToHex(commondBytes));
            link.setDevEUI(deviceId);
            link.setfPort(port);
            link.setReference("reference");

            CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
            HttpPost httpPost = new HttpPost(url);
            CloseableHttpResponse response = null;
            String out = null;
            JSONObject jsonObject = new JSONObject();//接收结果
            try {
                httpPost.setConfig(requestConfig);
                httpPost.setHeader("grpc-metadata-authorization", "设备授权码(硬件提供方提供)");

                StringEntity stringEntity = new StringEntity(JSON.toJSONString(link), "UTF-8");
                stringEntity.setContentEncoding("UTF-8");
                stringEntity.setContentType("application/json");
                httpPost.setEntity(stringEntity);


                response = httpclient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    out = EntityUtils.toString(response.getEntity(), "utf-8");
                    System.out.println(out + ",url: " + url + "params: " + JSON.toJSONString(link)); //打印错误信息
                } else {
                    out = EntityUtils.toString(response.getEntity(), "utf-8");
                }
                jsonObject = JSONObject.parseObject(out);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage() + ",url: " + url + "params: " + JSON.toJSONString(link)); //打印错误信息
                jsonObject.put("code", "1");
                jsonObject.put("message", e.getMessage());
            } finally {
                if (httpPost != null) {
                    httpPost.releaseConnection();
                }
            }


            if (jsonObject != null && jsonObject.size() > 0) {
                Object code = jsonObject.get("code");
                if (code != null) {
                    apiResult.setCode(Integer.parseInt(code.toString()));
                }
                Object error = jsonObject.get("error");
                if (error != null) {
                    apiResult.setMessage((String) error);
                } else {
                    apiResult.setMessage(JSONObject.toJSONString(jsonObject));
                }
            }
        } catch (Exception ex) {
            apiResult.setCode(-1);
            apiResult.setSuccess(false);
            apiResult.setMessage(ex.getMessage());
            throw ex;
        }
        return apiResult;
    }

    public static ApiResult doGet(String url, Map<String, String> json, String code) throws Exception {
        ApiResult apiResult = new ApiResult();
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        String out = null;
        JSONObject jsonObject = new JSONObject();//接收结果
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("grpc-metadata-authorization", code);

        StringEntity stringEntity = new StringEntity(JSON.toJSONString(json), "UTF-8");
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPost.setEntity(stringEntity);


        response = httpclient.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            out = EntityUtils.toString(response.getEntity(), "utf-8");
            System.out.println(out + ",url: " + url + "params: " + JSON.toJSONString(json)); //打印错误信息
        } else {
            out = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        jsonObject = JSONObject.parseObject(out);
        if (jsonObject != null && jsonObject.size() > 0) {
            Object code1 = jsonObject.get("code");
            if (code1 != null) {
                apiResult.setCode(Integer.parseInt(code1.toString()));
            }
            Object error = jsonObject.get("error");
            if (error != null) {
                apiResult.setMessage((String) error);
            } else {
                apiResult.setMessage(JSONObject.toJSONString(jsonObject));
            }
        }
        return apiResult;
    }
}
