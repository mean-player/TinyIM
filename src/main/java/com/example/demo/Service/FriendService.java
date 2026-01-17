package com.example.demo.Service;


import com.example.demo.Entity.Friend;
import com.example.demo.Entity.ReadRecord;
import com.example.demo.Repository.FriendRepository;
import com.example.demo.Repository.ReadRecordRepository;
import com.example.demo.VO.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final StringRedisTemplate redisTemplate;
    private final MessageCacheService messageCacheService;
    private final UserService userService;
    private final ReadRecordRepository readRecordRepository;

    private String buildKey(Long userId){
        return "friends_id:"+userId;
    }

    private String buildConversationId(Long userId_1,Long userId_2){
        return Math.min(userId_1,userId_2)+"T"+Math.max(userId_1,userId_2);
    }


    private void addRedisFriend(Long userId,Long friendId){
        String key_1 = buildKey(userId);
        String key_2 = buildKey(friendId);
        redisTemplate.opsForSet().add(key_1,String.valueOf(friendId));
        redisTemplate.opsForSet().add(key_2,String.valueOf(userId));
    }

    private void removeRedisFriend(Long userId,Long friendId){
        String key_1 = buildKey(userId);
        String key_2 = buildKey(friendId);
        redisTemplate.opsForSet().remove(key_1,String.valueOf(friendId));
        redisTemplate.opsForSet().remove(key_2,String.valueOf(userId));
    }
    public void addFriend(Long user_id,Long friend_id){
        Friend friend = new Friend(user_id,friend_id,System.currentTimeMillis());
        String conversationId = buildConversationId(user_id,friend_id);
        readRecordRepository.insertReadRecord(user_id,conversationId,-1L);
        readRecordRepository.insertReadRecord(friend_id,conversationId,-1L);
        friendRepository.addFriend(friend);
        addRedisFriend(user_id,friend_id);
    }

    public void removeFriend(Long userId,Long friendId){
        friendRepository.removeFriend(userId,friendId);
        String conversationId = buildConversationId(userId,friendId);
        readRecordRepository.removeReadRecord(userId,conversationId);
        readRecordRepository.removeReadRecord(friendId,conversationId);

        removeRedisFriend(userId,friendId);
    }

    public List<UserInfo>getFriendList(Long user_id){
        Set<String>friendIds_str = messageCacheService.getFriendIds(user_id);
        List<Long>friendIds = new ArrayList<>();
        for(String friend_id_str:friendIds_str){
            Long friend_id = Long.valueOf(friend_id_str);
            friendIds.add(friend_id);
        }
        return userService.batchGetUserInfo(friendIds);

    }

}
