package com.example.demo.Service;


import com.example.demo.Entity.Message;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.GroupMemberRepository;
import com.example.demo.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCacheService {

    private final FriendRepository friendRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MessageRepository messageRepository;
    private final StringRedisTemplate stringRedisTemplate;

    // 限制次数
    private static final int MAX_FALLBACK = 5;
    // 过期时间（分钟）
    private static final long EXPIRE_MINUTES = 1;

    //消息缓存至Redis三天
    private static final long EXPIRE_SECONDS = 3 * 24 * 3600; // 3天

    private String key(Long messageId) {
        return "messageCache:" + messageId;
    }


    //put relations into cache
    public void cacheRelations(Long userId){
        List<Long> FriendIds = friendRepository.selectFriends(userId);
        List<Long> GroupIds = groupMemberRepository.selectGroups(userId);
        for(Long f : FriendIds) {
            stringRedisTemplate.opsForSet().add("friends_id:" + userId, String.valueOf(f));
        }
        for(Long f : GroupIds) {
            stringRedisTemplate.opsForSet().add("groups_id:" + userId, String.valueOf(f));
        }
    }

    //fallback
    //从数据库读好友和群组关系 有fallback 频次限制
    private boolean tryFallback(Long sender_id,Long receiver_id,String relation_type) {
        String key = "fallback:limit:" + sender_id;
        // 增加计数，如果不存在则初始化为1
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null) {
            // 每次更新 value 时刷新过期时间为 EXPIRE_MINUTES
            stringRedisTemplate.expire(key, EXPIRE_MINUTES, TimeUnit.MINUTES);
            if (count > MAX_FALLBACK) {
                // 超过阈值
                return false;
            }else {
                cacheRelations(sender_id);
                if(relation_type.equals("FRIEND")) {
                    return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("friends_id:" + sender_id,
                            String.valueOf(receiver_id)));
                }else {
                    return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("groups_id:" + sender_id,
                            String.valueOf(receiver_id)));
                }
            }
        } else {
            // 增加失败（极少出现）
            return false;
        }

    }


    //verify relations status  检查是否有消息发送权限
    public boolean hasRelations(Long user_id,Long member_id,String relation_type){
        if(relation_type.equals("FRIEND") &&
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("friends_id:" + user_id,
                        String.valueOf(member_id)))) {
            return true;
        }else if(relation_type.equals("GROUP") &&
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("groups_id:" + user_id,
                        String.valueOf(member_id)))){
            return true;
        } else{
            return tryFallback(user_id,member_id,relation_type);
        }

    }


    //检查是否好友或群组成员  无需fallback
    public boolean hasRelationsNoDb(Long user_id,Long member_id,String relation_type){
        log.info("{} {} {}",user_id,member_id,relation_type);
        if(relation_type.equals("FRIEND") &&
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("friends_id:" + user_id, String.valueOf(member_id)))) {
            return true;
        }else return relation_type.equals("GROUP") &&
                Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("groups_id:" + user_id, String.valueOf(member_id)));
    }

    //获取所有好友id
    public Set<String> getFriendIds(Long user_id){
        String key = "friends_id:"+user_id;
        if(!stringRedisTemplate.hasKey(key)){
            cacheRelations(user_id);
        }
        Set<String>friendIds = stringRedisTemplate.opsForSet().members(key);
        if(friendIds == null) friendIds = Collections.emptySet();
        return friendIds;
    }

    //获取所有加入的群组的id
    public Set<String> getGroupIds(Long user_id){
        String key = "groups_id:"+user_id;
        if(!stringRedisTemplate.hasKey(key)){
            cacheRelations(user_id);
        }
        Set<String>groupIds = stringRedisTemplate.opsForSet().members(key);
        if(groupIds == null) groupIds = Collections.emptySet();
        return groupIds;
    }





    //缓存消息
    public void cacheMessage(Message msg) {

        Map<String, String> map = new HashMap<>();

        map.put("id", String.valueOf(msg.getId()));
        map.put("seq", String.valueOf(msg.getSeq()));
        map.put("conversation_id", msg.getConversation_id());
        map.put("content", msg.getContent());
        map.put("type", msg.getType());
        map.put("sender_id", String.valueOf(msg.getSender_id()));
        map.put("receiver_id", String.valueOf(msg.getReceiver_id()));
        map.put("send_time", msg.getSend_time().toString());

        String redisKey = key(msg.getId());

        stringRedisTemplate.opsForHash().putAll(redisKey, map);
        stringRedisTemplate.expire(redisKey, Duration.ofSeconds(EXPIRE_SECONDS));
    }

    /**
     * 从 Redis 读取 Message（Hash → 对象）
     */
    public Message getMessageById(Long messageId) {
        String redisKey = key(messageId);

        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(redisKey);
        if (map.isEmpty()){
            return messageRepository.selectById(messageId);
        }

        return new Message(Long.valueOf((String) map.get("id")),Long.valueOf((String) map.get("seq")),(String) map.get("conversation_id"),
                (String) map.get("content"), (String) map.get("type"),Long.valueOf((String) map.get("sender_id")),
                Long.valueOf((String) map.get("receiver_id")),Long.valueOf((String) map.get("send_time")));
    }


























}
