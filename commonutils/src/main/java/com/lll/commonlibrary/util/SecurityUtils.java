package com.lll.commonlibrary.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by root on 15-8-31.
 * 加密工具类
 */
public class SecurityUtils {


    /**
     * SHA1加密
     *
     * @param originStr 源字符串
     * @return 加密后字符串
     * @throws Exception
     */
    public String getSHA1Digest(String originStr) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = digest.digest(originStr.getBytes());
        StringBuilder sign = new StringBuilder();
        //将字符串转为大写
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();

    }


    /**
     * MD5加密
     *
     * @param s
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static final String md5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        byte[] inputBytes = s.getBytes("UTF-8");
        MessageDigest mdInst = MessageDigest.getInstance("md5");
        mdInst.update(inputBytes);
        byte[] md = mdInst.digest();
        int j = md.length;
        char[] str = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; ++i) {
            byte byte0 = md[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0XFF];
            str[k++] = hexDigits[byte0 & 0XFF];
        }
        return new String(str);
    }


}
