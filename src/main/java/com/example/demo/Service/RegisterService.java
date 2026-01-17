package com.example.demo.Service;

import com.example.demo.Component.DefaultProperties;
import com.example.demo.Component.EmailSender;
import com.example.demo.Component.SnowflakeIdUtils;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Tools.CodeUtil;
import com.example.demo.Tools.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;

    private final EmailSender emailSender;

    private final DefaultProperties defaultProperties;

    private final SnowflakeIdUtils idGenerator;

    private final StringRedisTemplate redisTemplate;

    private String generateAccount() {

        while (true) {
            String account = "u" + CodeUtil.randomNumeric(10);

            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent("acc:" + account, "1", 30, TimeUnit.DAYS);

            if (Boolean.TRUE.equals(ok)) {
                return account;   // 只有第一次写入成功时，才认为是唯一账号
            }
        }
    }


    public String sendEmail(String emailAddr){
        String uuid = UUID.randomUUID().toString().replace("-","");
        String code = CodeUtil.generateCode();
        String redisKey = "email:code:"+uuid;
        String limitKey = "email:limit:" + emailAddr;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(success)) {
            return ""; //太频繁
        }
        redisTemplate.opsForValue().set(redisKey,code,5, TimeUnit.MINUTES);
        emailSender.sendEmail(emailAddr,code);
        return uuid;
    }


    public ApiResponse<String> register(String uuid, String code, String emailAddr, String name, String password){

        String redisKey = "email:code:"+uuid;
        String realCode = redisTemplate.opsForValue().get(redisKey);
        if(realCode == null){
            log.error("验证码已过期");
            return ApiResponse.fail("验证码已过期");
        }
        if(!realCode.equals(code)){
            log.error("验证码不正确");
            return ApiResponse.fail("验证码不正确");
        }

        if(userRepository.existByEmail(emailAddr)){
            log.error("邮箱已被注册");
            return ApiResponse.fail("邮箱已被注册");
        }
        Long userId = idGenerator.nextId();
        String account = generateAccount();
        redisTemplate.opsForValue().set("account:"+account,String.valueOf(userId));//缓存映射
        User user = new User(userId,account,emailAddr,name,PasswordUtil.hash(password),defaultProperties.getUserAvatar(),
                System.currentTimeMillis());
        log.info("before insert");
        int rows = userRepository.addAUser(user);
        log.info("{}",rows);
        log.info("成功注册用户，account为{}",account);
        return ApiResponse.success(account);
    }

    public String beforeResetPassword(Long userId){
        String emailAddr = userRepository.selectEmailById(userId);
        if(emailAddr == null){
            return null;
        }
        return sendEmail(emailAddr);
    }

    public ApiResponse<String> resetPassword(String uuid, String code, Long userId, String newPassword){
        String redisKey = "email:code:"+uuid;
        String realCode = redisTemplate.opsForValue().get(redisKey);
        if(realCode == null){
            return ApiResponse.fail("验证码已过期");
        }
        if(!realCode.equals(code)){
            return ApiResponse.fail("验证码不正确");
        }
        userRepository.updatePassword(userId,PasswordUtil.hash(newPassword));
        return ApiResponse.success();
    }
}
