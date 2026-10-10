package utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SaltUtil {
    /**
     * 生成用户随机盐
     * @param byteLength 字节数，推荐16
     * @return base64编码盐字符串，存入数据库
     */
    public static String generateSalt(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[byteLength];
        secureRandom.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}

