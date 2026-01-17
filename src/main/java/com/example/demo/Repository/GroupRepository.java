package com.example.demo.Repository;

import com.example.demo.Mapper.GroupMapper;
import com.example.demo.VO.GroupShortInfo;
import com.example.demo.Entity.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupRepository {

    private final GroupMapper groupMapper;
    public void createGroup(Group group){
        groupMapper.createGroup(group);
    }

    public void disbandGroup(Long groupId){
        groupMapper.disbandGroup(groupId);
    }

    public Long findOwner(Long groupId){
        return groupMapper.findOwner(groupId);
    }

    public void updateName(Long groupId,String newName){
        groupMapper.updateName(groupId,newName);
    }

    public void updateSignature(Long groupId,String newSignature){
        groupMapper.updateSignature(groupId,newSignature);
    }

    public void updateAvatar(Long groupId,String newAvatar){
        groupMapper.updateAvatar(groupId,newAvatar);
    }

    public void increaseCount(Long groupId){
        groupMapper.increaseCount(groupId);
    }

    public void decreaseCount(Long groupId){
        groupMapper.decreaseCount(groupId);
    }

    //vm
    public GroupShortInfo selectById(Long groupId){
        return groupMapper.selectVOById(groupId);
    }




}
