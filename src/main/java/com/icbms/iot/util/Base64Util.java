package com.icbms.iot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.util.Base64;

public class Base64Util {

    private static final Logger logger = LoggerFactory.getLogger(Base64Util.class);

    private static Base64.Decoder decoder = Base64.getDecoder();

    private static Base64.Encoder encoder = Base64.getEncoder();

    public static String encrypt(byte[] bytes) {
        return (new BASE64Encoder()).encodeBuffer(bytes);
    }

    public static byte[] decrypt(String key) {
        try {
            return (new BASE64Decoder()).decodeBuffer(key);
        } catch (IOException e) {
            logger.error("error occurs when decrypt with base 64");
            e.printStackTrace();
        }

        return null;
    }

    public static String encodeToString(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }
}
