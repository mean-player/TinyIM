package com.example.demo.Repository;

import com.example.demo.Entity.Friend;

import com.example.demo.Mapper.FriendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepository {
    private final FriendMapper friendMapper;

    public List<Long> selectFriends(Long userId){
        return friendMapper.selectFriendIds(userId);
    }
    public void addFriend(Friend friend){
        friendMapper.addFriend(friend.getUserId_1(),friend.getUserId_2(),friend.getCreate_time());
    }
    public void removeFriend(Long userId_1,Long userId_2){
        friendMapper.removeFriend(userId_1,userId_2);
    }



}
