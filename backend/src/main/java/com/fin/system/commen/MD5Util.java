package com.fin.system.commen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String computeMD5Hash(String password) {
        try {
            // 创建 MessageDigest 实例，并指定算法为 MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 将密码字符串转换为字节数组
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

            // 计算字节数组的 MD5 散列值
            byte[] hashBytes = md.digest(passwordBytes);

            // 将散列值转换为十六进制字符串
            StringBuilder hexStringBuilder = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexStringBuilder.append('0');
                }
                hexStringBuilder.append(hex);
            }

            return hexStringBuilder.toString();
        } catch (NoSuchAlgorithmException ex) {
            // 处理算法不支持的异常
            ex.printStackTrace();
        }

        return null;
    }

}
