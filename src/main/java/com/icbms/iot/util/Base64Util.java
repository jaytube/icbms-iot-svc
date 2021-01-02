package com.icbms.iot.util;

import com.icbms.iot.client.MqttPushClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

public class Base64Util {

    private static final Logger logger = LoggerFactory.getLogger(MqttPushClient.class);

    private static String encrypt(byte[] bytes){
        return (new BASE64Encoder()).encodeBuffer(bytes);
    }

    private static byte[] decrypt(String key) {
        try {
            return (new BASE64Decoder()).decodeBuffer(key);
        } catch (IOException e) {
            logger.error("error occurs when decrypt with base 64");
            e.printStackTrace();
        }

        return null;
    }
}
