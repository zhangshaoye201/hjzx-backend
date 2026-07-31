package utils;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256加密工具类
 */
public class SHA256Util {

	/**
	 * SHA256 加盐哈希
	 * @param rawStr 明文密码
	 * @param salt 后端配置盐
	 * @return sha256 十六进制哈希
	 */
	public static String sha256Encrypt(String rawStr, String salt) {
	    if (rawStr == null || rawStr.isBlank()) {
	        throw new IllegalArgumentException("明文不能为空");
	    }
	    if (salt == null) {
	        throw new IllegalArgumentException("盐值不能为空");
	    }
	    try {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        // 明文拼接盐
	        byte[] hash = md.digest((rawStr + salt).getBytes(StandardCharsets.UTF_8));
	        StringBuilder hexStr = new StringBuilder();
	        for (byte b : hash) {
	            String hex = Integer.toHexString(0xff & b);
	            if (hex.length() == 1) {
	                hexStr.append('0');
	            }
	            hexStr.append(hex);
	        }
	        return hexStr.toString();
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("SHA256加密失败", e);
	    }
	}
}
