package com.example.demo.Service;


import com.example.demo.DTO.UserSearch;
import com.example.demo.Entity.Storage;
import com.example.demo.Repository.StorageRepository;
import com.example.demo.VO.UserInfo;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient; // 使用Redisson做分布式锁

    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final MessageCacheService messageCacheService;

    private static final String CACHE_KEY_PREFIX = "userInfo:";
    private static final long EXPIRE_TIME = 1L; // 1天
    private static final TimeUnit EXPIRE_UNIT = TimeUnit.DAYS;
    private static final String NULL_MARK_FIELD = "_isNull";


    // --- 辅助方法：Map 与 Object 转换 ---
    private UserInfo mapToUserInfo(Long id, Map<Object, Object> entries) {
        return new UserInfo(id,(String) entries.get("account"),(String) entries.get("name"),
                (String) entries.get("avatar"),(String) entries.get("signature"));
    }

    private Map<String, String> userInfoToMap(UserInfo user) {
        Map<String, String> map = new HashMap<>();
        if (user.getAccount() != null) map.put("account", user.getAccount());
        if (user.getName() != null) map.put("name", user.getName());
        if (user.getAvatar() != null) map.put("avatar", user.getAvatar());
        if (user.getSignature() != null) map.put("signature", user.getSignature());
        return map;
    }

    // 为 Pipeline 准备的 Byte Map
    private Map<byte[], byte[]> userInfoToByteMap(UserInfo user) {
        Map<byte[], byte[]> map = new HashMap<>();
        if (user.getAccount() != null) map.put("account".getBytes(), user.getAccount().getBytes());
        if (user.getName() != null) map.put("name".getBytes(), user.getName().getBytes());
        // ... 其他字段
        return map;
    }


    //=================================================METHOD===========================================================


    //获取单个用户信息（含缓存、锁、防穿透）
    public UserInfo getUserInfo(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (!entries.isEmpty()) {
            if (entries.containsKey(NULL_MARK_FIELD)) {
                redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
                log.warn("此用户为空，在此防穿刺");
                return null;
            }
            UserInfo userInfo = mapToUserInfo(userId, entries);
            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
            log.info("redis找到用户");
            return userInfo;
        }

        String lockKey = "lock:user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        int retry = 0;

        while (retry <= 5) {
            try {
                boolean isLocked = lock.tryLock(500, 10000, TimeUnit.MILLISECONDS);
                if (isLocked) {
                    try {
                        entries = redisTemplate.opsForHash().entries(key);
                        if (!entries.isEmpty()) {
                            if (entries.containsKey(NULL_MARK_FIELD)) {
                                return null;
                            }
                            return mapToUserInfo(userId, entries);
                        }

                        UserInfo userInfo = userRepository.selectInfoById(userId);
                        if (userInfo != null) {
                            Map<String, String> hash = userInfoToMap(userInfo);
                            redisTemplate.opsForHash().putAll(key, hash);
                            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
                            log.info("数据库查到用户信息，并缓存");
                            return userInfo;
                        } else {
                            redisTemplate.opsForHash().put(key, NULL_MARK_FIELD, "true");
                            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
                            log.warn("此用户不存在，在此设空标防穿刺");
                            return null;
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    retry++;
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }


    //批量获取用户信息 (Pipeline 优化)
    public List<UserInfo> batchGetUserInfo(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();

        // 去重
        List<Long> uniqueIds = userIds.stream().distinct().toList();
        List<UserInfo> resultList = new ArrayList<>();
        List<Long> missIds = new ArrayList<>();

        // 1. 使用 Pipeline 批量读取 Redis
        List<Object> pipelineResults = redisTemplate.executePipelined((RedisCallback<Object>)  connection-> {
            for (Long id : uniqueIds) {
                String key = CACHE_KEY_PREFIX + id;
                connection.hashCommands().hGetAll(key.getBytes());
                // 顺便批量刷新过期时间（Best Effort）
                connection.keyCommands().expire(key.getBytes(), EXPIRE_UNIT.toSeconds(EXPIRE_TIME));

            }
            return null;
        });

        // 2. 解析 Pipeline 结果
        for (int i = 0; i < uniqueIds.size(); i++) {
            Long userId = uniqueIds.get(i);
            @SuppressWarnings("unchecked")
            Map<Object, Object> entries = (Map<Object, Object>) pipelineResults.get(i); // hGetAll 结果

            if (entries == null || entries.isEmpty()) {
                missIds.add(userId);
            } else {
                if (entries.containsKey(NULL_MARK_FIELD)) {
                    // 缓存了空对象，结果集里放null或者跳过，视业务需求定
                    resultList.add(null);
                } else {
                    resultList.add(mapToUserInfo(userId, entries));
                }
            }
        }

        // 3. 处理缓存未命中的 ID (批量查库)
        if (!missIds.isEmpty()) {
            List<UserInfo> userInfos = userRepository.selectByIdList(missIds);
            Map<Long, UserInfo> dbUserMap = userInfos.stream()
                    .collect(Collectors.toMap(UserInfo::getId, u -> u));

            // 4. 将查到的数据回写 Redis (使用 Pipeline 批量写)
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long missId : missIds) {
                    UserInfo u = dbUserMap.get(missId);
                    String key = CACHE_KEY_PREFIX + missId;

                    if (u != null) {
                        Map<byte[], byte[]> hash = userInfoToByteMap(u);
                        connection.hashCommands().hMSet(key.getBytes(),hash);
                    } else {
                        // 数据库也没有，批量写入空对象
                        connection.hashCommands().hSet(key.getBytes(), NULL_MARK_FIELD.getBytes(), "true".getBytes());
                    }
                    connection.keyCommands().expire(key.getBytes(), EXPIRE_UNIT.toSeconds(EXPIRE_TIME));
                }
                return null;
            });

            // 将 DB 查到的结果加入最终返回列表
            // 注意：这里需要保证返回顺序或仅返回存在的，视业务逻辑而定
            for (Long missId : missIds) {
                resultList.add(dbUserMap.get(missId)); // 如果是null也会add null
            }
        }

        return resultList;
    }


    //修改用户信息，使用旁路加载
    public void changeAvatar(Long userId,String newAvatar){
        String key = CACHE_KEY_PREFIX+userId;
        userRepository.updateAvatar(userId,newAvatar);
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
        }
    }

    public void changeSignature(Long user_id,String newSignature){
        String key = CACHE_KEY_PREFIX+user_id;
        userRepository.updateSignature(user_id,newSignature);
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
        }
    }

    public void changeName(Long user_id,String newName){
        String key = CACHE_KEY_PREFIX+user_id;
        userRepository.updateName(user_id,newName);
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
        }
    }

    //查看用户信息
    private Long getIdByAccount(String account){
        String user_id_str = redisTemplate.opsForValue().get("account:"+account);
        if(user_id_str == null){//redis中没有
            Long user_id = userRepository.selectIdByAccount(account);
            if(user_id == null){//伪造用户
                redisTemplate.opsForValue().set("account:"+account,NULL_MARK_FIELD, Duration.ofDays(1));
                return null;
            }else{//真实用户
                redisTemplate.opsForValue().set("account:"+account,String.valueOf(user_id));
                return user_id;
            }
        } else if (user_id_str.equals(NULL_MARK_FIELD)) {//查询伪造用户
            return null;
        }else{//redis查到用户
            return Long.valueOf(user_id_str);
        }
    }


    //根据account查询用户信息以及关系
    public UserSearch searchByAccount(String account,Long user_id){
        Long id = getIdByAccount(account);
        if(id == null){
            log.info("查不到id，该用户不存在");
            return null;
        }
        UserInfo userInfo = getUserInfo(id);
        if(userInfo == null){
            log.warn("未查到用户");
            return null;
        }
        log.info("{}",userInfo.getAccount());
        boolean isFriend = messageCacheService.hasRelationsNoDb(user_id,id,"FRIEND");
        log.info("{}",isFriend);
        UserSearch userSearch = new UserSearch();
        userSearch.setUserInfo(userInfo);
        userSearch.setFriend(isFriend);
        return userSearch;
    }

    //根据userid获得storage列表
    public List<Storage>getStorage(Long userId,String type,Integer limit,Integer offset){
        if(type.equals("all")){
            return storageRepository.selectStorage(userId,null,limit,offset);
        }
        return storageRepository.selectStorage(userId,type,limit,offset);

    }


}
