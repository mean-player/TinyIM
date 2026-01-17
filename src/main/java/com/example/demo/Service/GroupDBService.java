package com.example.demo.Service;

import com.example.demo.Entity.GroupMember;
import com.example.demo.Repository.GroupMemberRepository;
import com.example.demo.Repository.GroupRepository;
import com.example.demo.Repository.ReadRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupDBService {

    private final GroupMemberRepository groupMemberRepository;
    private final ReadRecordRepository readRecordRepository;
    private final GroupRepository groupRepository;

    @Transactional
    public void addMemberDB(GroupMember groupMember){
        groupMemberRepository.addGroupMember(groupMember);
        groupRepository.increaseCount(groupMember.getGroup_id());
        readRecordRepository.insertReadRecord(groupMember.getUser_id(),String.valueOf(groupMember.getGroup_id()),-1L);
    }

    @Transactional
    public void removeMemberDB(Long memberId,Long groupId){
        groupMemberRepository.removeMember(memberId,groupId);
        groupRepository.decreaseCount(groupId);
        readRecordRepository.removeReadRecord(memberId,String.valueOf(groupId));
    }

    @Transactional
    public void disbandGroupDB(Long groupId){
        groupMemberRepository.disbandGroup(groupId);
        groupRepository.disbandGroup(groupId);

    }
}
