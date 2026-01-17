package com.example.demo.Service;

import com.example.demo.Component.JwtService;
import com.example.demo.DTO.LocalSeq;
import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.TokenResp;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Tools.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final TokenService tokenService;
    private final JwtService jwtService;
    private final MessageCacheService messageCacheService;
    private final RegisterService registerService;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ReadRecordService readRecordService;
    private final TaskExecutor taskExecutor;

    //返回此邮件的uuid任务号
    public String sendLoginEmail(String emailAddr){
        return registerService.sendEmail(emailAddr);
    }


    private void cacheAccountAId(User user){
        if(stringRedisTemplate.opsForValue().get("account:"+user.getAccount()) == null){
            stringRedisTemplate.opsForValue().set("account:"+user.getAccount(),String.valueOf(user.getId()));
        }
    }

    public String beforeLoginByEmail(String email_addr){
        return sendLoginEmail(email_addr);
    }

    private TokenResp afterLogin(User user){
        CompletableFuture.runAsync(() ->{
            String uuid = UUID.randomUUID().toString();
            tokenService.saveLoginSession(user.getId(),uuid);
            messageCacheService.cacheRelations(user.getId());//缓存好友关系
            readRecordService.cacheUserReadRecords(user.getId());//缓存已读信息的seq
            cacheAccountAId(user);//缓存account和userId的映射

        },taskExecutor);

        TokenResp tokenResp = new TokenResp();
        tokenResp.setAccessToken(jwtService.generateAccessToken(user.getId(),user.getAccount()));
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        tokenResp.setRefreshToken(refreshToken);
        Map<String,Object>userMap = new HashMap<>();
        userMap.put("userId",String.valueOf(user.getId()));
        userMap.put("account",String.valueOf(user.getAccount()));
        stringRedisTemplate.opsForHash().putAll("refreshToken:"+refreshToken,userMap);
        return tokenResp;

    }


    public TokenResp login(LoginRequest req){
        switch (req.getLoginType()){
            case ACCOUNT_PASSWORD -> {
                log.info("login by ACCOUNT_PASSWORD account={}", req.getAccount());
                User user = userRepository.selectByAccount(req.getAccount());
                if(PasswordUtil.verify(req.getPassword(),user.getPassword())){
                    log.info("password verified userId={}", user.getId());
                    return afterLogin(user);
                }
                else{
                    log.info("password mismatch account={}", req.getAccount());
                    return null;
                }
            }
            case EMAIL_CODE -> {
                log.info("login by EMAIL_CODE email={}", req.getEmail());
                String redisKey = "email:code:"+req.getUuid();
                String realCode = stringRedisTemplate.opsForValue().get(redisKey);
                if(realCode == null){
                    log.info("email code expired uuid={}", req.getUuid());
                    return null;
                }
                if(!realCode.equals(req.getCode())){
                    log.info("email code mismatch uuid={}", req.getUuid());
                    return null;
                }

                if(!userRepository.existByEmail(req.getEmail())){
                    log.info("email not registered email={}", req.getEmail());
                    return null;
                }

                User user = userRepository.selectByEmailAddr(req.getEmail());
                return afterLogin(user);

            }

        }
        return null;
    }

    private long parseSenderId(String conversationId){
        if(conversationId.contains("T")){
            return Long.parseLong(conversationId.split("T")[0]);
        }else{
            return Long.parseLong(conversationId);
        }
    }

    public List<LocalSeq>getLocalSeq(Long userId){
        String key = "readRecord:"+userId;
        if(!stringRedisTemplate.hasKey(key)){
            readRecordService.cacheUserReadRecords(userId);
        }
        Map<Object,Object>map = stringRedisTemplate.opsForHash().entries(key);
        List<LocalSeq>localSeqList = new ArrayList<>();
        for(Map.Entry<Object,Object> e : map.entrySet()){
            LocalSeq localSeq = new LocalSeq();
            localSeq.setSender_id(parseSenderId((String) e.getKey()));
            localSeq.setSeq(Long.parseLong((String) e.getValue()));
            localSeqList.add(localSeq);
        }
        return localSeqList;
    }





























}
