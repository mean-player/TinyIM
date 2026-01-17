package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Repository.GroupRepository;
import com.example.demo.VO.GroupShortInfo;
import com.example.demo.VO.UserInfo;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GroupSearchService {
    private final GroupRepository groupRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    private static final String CACHE_KEY_PREFIX = "groupInfo:";
    private static final long EXPIRE_TIME = 1L; // 1天
    private static final TimeUnit EXPIRE_UNIT = TimeUnit.DAYS;
    private static final String NULL_MARK_FIELD = "_isNull";


    // --- 辅助方法：Map 与 Object 转换 ---
    private GroupShortInfo mapToGroupShortInfo(Long id, Map<Object, Object> entries) {
        return new GroupShortInfo(id,(String) entries.get("name"),
                (String) entries.get("avatar"),(String) entries.get("signature"),(Integer)entries.get("member_count"));
    }

    private Map<String, String> groupShortInfoToMap(GroupShortInfo groupShortInfo) {
        Map<String, String> map = new HashMap<>();
        if (groupShortInfo.getName() != null) map.put("name", groupShortInfo.getName());
        if (groupShortInfo.getAvatar() != null) map.put("avatar", groupShortInfo.getAvatar());
        if (groupShortInfo.getSignature() != null) map.put("signature", groupShortInfo.getSignature());
        return map;
    }


    //获取单个群组信息（含缓存、锁、防穿透）
    public GroupShortInfo getGroupShortInfo(Long groupId) {
        String key = CACHE_KEY_PREFIX + groupId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (!entries.isEmpty()) {
            if (entries.containsKey(NULL_MARK_FIELD)) {
                redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
                return null;
            }
            GroupShortInfo groupShortInfo = mapToGroupShortInfo(groupId, entries);
            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
            return groupShortInfo;
        }

        String lockKey = "lock:group:" + groupId;
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
                            return mapToGroupShortInfo(groupId, entries);
                        }

                        GroupShortInfo groupShortInfo = groupRepository.selectById(groupId);
                        if (groupShortInfo != null) {
                            Map<String, String> hash = groupShortInfoToMap(groupShortInfo);
                            redisTemplate.opsForHash().putAll(key, hash);
                            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
                            return groupShortInfo;
                        } else {
                            redisTemplate.opsForHash().put(key, NULL_MARK_FIELD, "true");
                            redisTemplate.expire(key, EXPIRE_TIME, EXPIRE_UNIT);
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


}
