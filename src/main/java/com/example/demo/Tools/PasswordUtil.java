package com.example.demo.Tools;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * 密码工具类 —— 使用 Argon2 加密和校验
 */
public class PasswordUtil {

    // 可以调整参数：盐长度、哈希长度、迭代次数、内存成本、并行度
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 3;
    private static final int MEMORY = 65536; // KB
    private static final int PARALLELISM = 1;

    private static final Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(
            SALT_LENGTH, HASH_LENGTH, ITERATIONS, MEMORY, PARALLELISM
    );


    //对明文密码进行不可逆加密

    public static String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }


    //校验明文密码和加密后的哈希是否匹配
    public static boolean verify(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

}