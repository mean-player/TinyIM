package com.example.demo.Mapper;


import com.example.demo.Entity.Friend;
import com.example.demo.Provider.FriendProvider;
import org.apache.ibatis.annotations.*;

        import java.util.List;

@Mapper
public interface FriendMapper {

    /** 查询所有好友（某人参与的任何好友关系） */
    @SelectProvider(type = FriendProvider.class, method = "selectFriendIds")
    List<Long> selectFriendIds(@Param("userId") Long userId);


    /** 添加好友 */
    @InsertProvider(type = FriendProvider.class, method = "addFriend")
    int addFriend(
            @Param("userId_1") Long userId1,
            @Param("userId_2") Long userId2,
            @Param("create_time") Long createTime
    );


    /** 删除好友（互删） */
    @DeleteProvider(type = FriendProvider.class, method = "removeFriend")
    int removeFriend(
            @Param("userId_1") Long userId1,
            @Param("userId_2") Long userId2
    );
}