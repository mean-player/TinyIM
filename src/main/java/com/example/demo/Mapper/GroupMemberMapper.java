package com.example.demo.Mapper;


import com.example.demo.Entity.GroupMember;
import com.example.demo.Provider.GroupMemberProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GroupMemberMapper {

    /** 查询用户加入的所有群 */
    @SelectProvider(type = GroupMemberProvider.class, method = "selectGroups")
    List<Long> selectGroups(@Param("user_id") Long userId);


    /** 查询群里的所有成员 */
    @SelectProvider(type = GroupMemberProvider.class, method = "selectMembers")
    List<Long> selectMembers(@Param("group_id") Long groupId);


    /** 添加群成员 */
    @InsertProvider(type = GroupMemberProvider.class, method = "addGroupMember")
    int addGroupMember(
            @Param("group_id") Long groupId,
            @Param("user_id") Long userId,
            @Param("join_time") Long joinTime,
            @Param("role") String role
    );


    /** 移除某个成员 */
    @DeleteProvider(type = GroupMemberProvider.class, method = "removeMember")
    int removeMember(
            @Param("group_id") Long groupId,
            @Param("user_id") Long userId
    );


    /** 解散群（删除所有成员） */
    @DeleteProvider(type = GroupMemberProvider.class, method = "disbandGroup")
    int disbandGroup(@Param("group_id") Long groupId);
}