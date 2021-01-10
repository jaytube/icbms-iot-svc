package com.icbms.iot.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.icbms.iot.ssl.ApiResult;
import com.icbms.iot.ssl.DownLink;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class HttpTest {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    private static String url = "https://10.0.210.41:8080/api/";

    private static Base64.Decoder decoder = Base64.getDecoder();
    private static Base64.Encoder encoder = Base64.getEncoder();

    /**JWT TOKEN值*/
    private static final String _jwt_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJsb3JhLWFwcC1zZXJ2ZXIiLCJhdWQiOiJ" +
            "sb3JhLWFwcC1zZXJ2ZXIiLCJuYmYiOjE1Mzc0MDgzMDMsImV4cCI6MzMwOTQzMTcxMDMsInN1YiI6I" +
            "nVzZXIiLCJ1c2VybmFtZSI6ImFkbWluIn0.14eVliflc5oG5FJXIphEfcWbc5A4DxzTk-u5AMaIsJc";

    private static final String contentType = "application/json; charset=utf-8";

    private static final int timeOut = 60;

    private static String convertObjectToJson(DownLink link) throws IOException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(link); //error on this line
        return json;

    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        // toUpperCase将字符串中的所有字符转换为大写
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        // toCharArray将此字符串转换为一个新的字符数组。
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



    public static void test() throws Exception {
        String hexContent = "000010E1010001020008";// 四层闪烁
        byte[] commondBytes = hexStringToBytes(hexContent);
        DownLink link = new DownLink();
        link.confirmed = false;
        link.data = new String(encoder.encodeToString(commondBytes));
        //String deviceid = "393235306537910b";
        String deviceid = "393235305c378d03";
        link.devEUI = deviceid;
        link.fPort = 4;
        link.reference = "reference";
        System.out.println(convertObjectToJson(link));

        String action = "nodes/" + deviceid + "/queue";

        HttpPost(url + action,convertObjectToJson(link));

    }

    /**
     * 发起Post请求
     * @param apiUrl    API地址
     * @param param     Json串
     * @return
     */
    public static ApiResult HttpPost(String apiUrl, String param) throws Exception{
        ApiResult apiResult = new ApiResult();
        try {
            logger.info("【Post】 URL = "+apiUrl);
            logger.info("入参="+param);
            JSONObject result = doPost(apiUrl, param, _jwt_token);
            logger.info("出参="+JSONObject.toJSONString(result));

            if(result != null && result.size() > 0){
                Object code =  result.get("code");
                if(code != null ){
                    apiResult.code = Integer.parseInt(code.toString());
                }
                Object error = result.get("error");
                if(error != null){
                    apiResult.message = (String)error;
                }
                apiResult.message = JSONObject.toJSONString(result);
            }
        }catch (Exception ex){
            //apiResult.Error(ex);
            apiResult.code = 1;
            apiResult.message = ex.getMessage();
            logger.error("请求失败" + "code = " + apiResult.code + ", message = " + apiResult.message , ex);
        }
        return apiResult;
    }

    /**
     * 发送 POST 请求
     *
     * @param url    API接口URL
     * @param params 参数map
     * @return
     */
    public static JSONObject doPost(String url, String params, String jwtToken) throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        String out = null;
        JSONObject jsonObject = new JSONObject();//接收结果
        try {
            httpPost.setConfig(requestConfig);
            httpPost.setHeader("grpc-metadata-authorization", jwtToken);
            StringEntity stringEntity = new StringEntity(params, "UTF-8");
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            logger.info("httppost: " + JSON.toJSONString(httpPost));
            response = httpclient.execute(httpPost);
            //logger.info("response: " + JSON.toJSONString(response));
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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage() + ",url: " + url + "params: " + params); //打印错误信息
            jsonObject.put("code", "1");
            jsonObject.put("message", e.getMessage());
        } finally {
            if (httpPost != null) {
                httpPost.releaseConnection();
            }
        }
        return jsonObject;
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
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

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
