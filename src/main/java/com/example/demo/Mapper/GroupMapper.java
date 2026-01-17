package com.example.demo.Mapper;


import com.example.demo.Entity.Group;
import com.example.demo.Provider.GroupProvider;
import com.example.demo.VO.GroupShortInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface GroupMapper {

    @InsertProvider(type = GroupProvider.class, method = "createGroup")
    void createGroup(Group group);

    @DeleteProvider(type = GroupProvider.class, method = "disbandGroup")
    void disbandGroup(@Param("groupId") Long groupId);

    @SelectProvider(type = GroupProvider.class, method = "findOwner")
    Long findOwner(@Param("groupId") Long groupId);

    @UpdateProvider(type = GroupProvider.class, method = "updateName")
    void updateName(@Param("groupId") Long groupId, @Param("newName") String newName);

    @UpdateProvider(type = GroupProvider.class, method = "updateSignature")
    void updateSignature(@Param("groupId") Long groupId, @Param("newSignature") String newSignature);

    @UpdateProvider(type = GroupProvider.class, method = "updateAvatar")
    void updateAvatar(@Param("groupId") Long groupId, @Param("newAvatar") String newAvatar);

    @UpdateProvider(type = GroupProvider.class, method = "increaseCount")
    void increaseCount(@Param("groupId") Long groupId);

    @UpdateProvider(type = GroupProvider.class, method = "decreaseCount")
    void decreaseCount(@Param("groupId") Long groupId);

    @SelectProvider(type = GroupProvider.class, method = "selectVOById")
    GroupShortInfo selectVOById(@Param("groupId") Long groupId);
}