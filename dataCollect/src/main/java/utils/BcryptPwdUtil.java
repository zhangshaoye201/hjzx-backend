package utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Bcrypt密码工具，用于用户密码加密/校验
 * 注意：加密时内部自动生成独立随机盐，返回字符串自带盐信息，数据库直接存返回字符串即可
 */
public class BcryptPwdUtil {

    // strength：哈希迭代强度，推荐10；数值越大越耗CPU，范围4‑31
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 加密明文密码
     * @param rawPassword 用户明文密码
     * @return bcrypt密文（内含随机盐，直接存入数据库userPwd）
     */
    public static String encrypt(String rawPassword) {
        if(rawPassword == null || rawPassword.isBlank()){
            throw new IllegalArgumentException("密码不能为空");
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码
     * @param rawPassword 用户登录输入的明文密码
     * @param dbPassword 数据库取出的bcrypt密文
     * @return true=密码匹配；false=密码错误
     */
    public static boolean matches(String rawPassword, String dbPassword) {
        if(rawPassword == null || dbPassword == null){
            return false;
        }
        return ENCODER.matches(rawPassword, dbPassword);
    }
}
