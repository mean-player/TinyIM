package com.example.demo.Repository;


import com.example.demo.Entity.GroupMember;
import com.example.demo.Mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepository {

    private final GroupMemberMapper groupMemberMapper;

    public List<Long> selectGroups(Long user_id){
        return groupMemberMapper.selectGroups(user_id);
    }

    public List<Long> selectMembers(Long group_id){
        return groupMemberMapper.selectMembers(group_id);
    }
    public void addGroupMember(GroupMember groupMember){
        groupMemberMapper.addGroupMember(groupMember.getGroup_id(),groupMember.getUser_id(),
                groupMember.getJoin_time(),groupMember.getRole());
    }
    public void removeMember(Long user_id,Long group_id){
        groupMemberMapper.removeMember(group_id,user_id);
    }
    public void disbandGroup(Long group_id){
        groupMemberMapper.disbandGroup(group_id);
    }
}
