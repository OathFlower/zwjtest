package cn.xunhou.web.xbbcloud.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class MD5Utils {


    /**
     * 加盐MD5
     *
     * @param password 原始密码
     * @return 加盐的MD5字符串
     * @author tireless
     */
    public static String generate(String password) {
//        生成随机盐，长度12位
        byte[] bytes = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);

        StringBuilder builder = new StringBuilder();
//        将字节数组变为字符串
        for (int i = 0; i < bytes.length; i++) {
//            将生成的值，全部映射到0-255 之间
            int val = ((int) bytes[i]) & 0xff;
            if (val < 16) {
//                为了控制盐的长度，这里小于16 的值，我们将它补为 大于16的值；
//                这样，生的盐的长度是固定的：bytes * 2 ;
                builder.append(Integer.toHexString(val + 16));
            } else {
                builder.append(Integer.toHexString(val));
            }
        }

//        最终的盐，长度是 12*2 = 24 ；
        String salt = builder.toString();

//        先加盐Md5一把，再将 MD5 转换成 24位的 base64 位编码
        password = md5Hex(password + salt);

        char[] cs = new char[salt.length() + password.length()];

        for (int i = 0; i < cs.length; i += 4) {

//            密码编码
            cs[i] = password.charAt(i / 2);
            cs[i + 2] = password.charAt(i / 2 + 1);
//            盐编码
            cs[i + 1] = salt.charAt(i / 2);
            cs[i + 3] = salt.charAt(i / 2 + 1);

        }
        return new String(cs);
    }

    /**
     * 校验加盐后是否和原文一致
     *
     * @param password
     * @param md5
     * @return true 代表密码验证通过
     * @author tireless
     */
    public static boolean verify(String password, String md5) {
//        解码密码
        char[] cs1 = new char[24];
//        解码盐
        char[] cs2 = new char[24];
//        从MD5 中取出盐
        for (int i = 0; i < md5.length(); i += 4) {
//            取出盐
            cs2[i / 2] = md5.charAt(i + 1);
            cs2[i / 2 + 1] = md5.charAt(i + 3);
//            取出密码的MD5值（经过Base64转换后的MD5）
            cs1[i / 2] = md5.charAt(i + 0);
            cs1[i / 2 + 1] = md5.charAt(i + 2);
        }

        String salt = new String(cs2);
        System.out.println(md5Hex(password + salt));

        return md5Hex(password + salt).equals(new String(cs1));
    }


    /**
     * 获取十六进制字符串形式的MD5摘要
     */
    private static String md5Hex(String src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(src.getBytes());
            return new String(Base64.getEncoder().encode(bs));
        } catch (Exception e) {
            return null;
        }
    }

    public static String stringToMD5(String plainText) {

        byte[] secretBytes = null;

        try {

            secretBytes = MessageDigest.getInstance("md5").digest(

                    plainText.getBytes());

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("没有这个md5算法！");

        }

        String md5code = new BigInteger(1, secretBytes).toString(16);

        for (int i = 0; i < 32 - md5code.length(); i++) {

            md5code = "0" + md5code;

        }

        return md5code;

    }
}
