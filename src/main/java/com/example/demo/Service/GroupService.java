package com.example.demo.Service;

import com.example.demo.Component.DefaultProperties;
import com.example.demo.Component.SnowflakeIdUtils;
import com.example.demo.DTO.CreateGroupReq;
import com.example.demo.Entity.Group;
import com.example.demo.Entity.GroupMember;
import com.example.demo.Repository.GroupMemberRepository;
import com.example.demo.Repository.GroupRepository;
import com.example.demo.Repository.ReadRecordRepository;
import com.example.demo.VO.GroupShortInfo;
import com.example.demo.VO.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupMemberRepository groupMemberRepository;
    private final StringRedisTemplate redisTemplate;
    private final GroupRepository groupRepository;
    private final SnowflakeIdUtils idGenerator;
    private final DefaultProperties defaultProperties;
    private final UserService userService;
    private final MessageCacheService messageCacheService;
    private final GroupDBService groupDBService;
    private final GroupSearchService groupSearchService;


    private static final int EXPIRE_HOURS = 24;

    private String buildGroupKey(Long groupId){
        return "group:members:"+groupId;
    }
    private String buildUserKey(Long userId){return "groups_id:"+userId;}

    private void refreshExpire(String key){
        redisTemplate.expire(key, Duration.ofHours(EXPIRE_HOURS));
    }

    private boolean isNotOwner(Long groupId,Long userId){
        Long owner_id = groupRepository.findOwner(groupId);
        return !owner_id.equals(userId);
    }

    private void invalidateCache(Long groupId){
        String key = "groupInfo:"+groupId;
        if(redisTemplate.hasKey(key)){
            redisTemplate.delete(key);
        }
    }




    private void cacheGroupMembers(Long groupId){
        String key = buildGroupKey(groupId);
        if(! redisTemplate.hasKey(key)){
            List<Long>members = groupMemberRepository.selectMembers(groupId);
            if(members.isEmpty()){
                return;
            }
            String[] ids = members.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(key,ids);
            refreshExpire(key);
        }
    }


    //创建群组
    public void createGroup(CreateGroupReq createGroupReq,Long user_id){
        Long groupId = idGenerator.nextId();
        Group group = new Group(groupId,createGroupReq.getName(),user_id,
                defaultProperties.getUserAvatar(), createGroupReq.getSignature(),1,System.currentTimeMillis());
        groupRepository.createGroup(group);
        addMembersToGroup(groupId,user_id);

    }


    //离开某个群组
    public boolean leaveGroup(Long userId,Long groupId){
        if(!isNotOwner(groupId,userId)){
            return false;//是群主，无法离开群组
        }
        if(messageCacheService.hasRelations(userId,groupId,"GROUP")){
            groupDBService.removeMemberDB(userId,groupId);
            String groupKey = buildGroupKey(groupId);
            String userKey = buildUserKey(userId);
            if(redisTemplate.hasKey(groupKey)){
                redisTemplate.opsForSet().remove(groupKey,String.valueOf(userId));
            }
            if(redisTemplate.hasKey(userKey)){
                redisTemplate.opsForSet().remove(userKey,String.valueOf(groupId));
            }
            return true;
        }else{
            return false;
        }
    }

    //获取群组成员id
    public List<Long>getMembers(Long groupId){
        String key = buildGroupKey(groupId);
        if(!redisTemplate.hasKey(key)) cacheGroupMembers(groupId);//如果没key则fallback
        Set<String>members = redisTemplate.opsForSet().members(key);
        if(members != null && !members.isEmpty()){
            refreshExpire(key);//再次刷新
            return members.stream().map(Long::valueOf).collect(Collectors.toList());
        }
        return null;
    }


    //增加成员到群组
    public void addMembersToGroup(Long groupId,Long userId){
        String key_1 = buildGroupKey(groupId);
        String key_2 = buildUserKey(userId);
        GroupMember groupMember = new GroupMember(groupId,userId,System.currentTimeMillis(),"member");
        groupDBService.addMemberDB(groupMember);
        if(redisTemplate.hasKey(key_1)) {
            redisTemplate.opsForSet().add(key_1, String.valueOf(userId));
        }
        if(redisTemplate.hasKey(key_2)){
            redisTemplate.opsForSet().add(key_2,String.valueOf(groupId));
        }
    }


    //从群组移出成员
    public void removeMembersFromGroup(Long groupId,Long memberId,Long user_id){
        if(isNotOwner(groupId,user_id)){
            return;//不是群主，无权限
        }
        String key_1 = buildGroupKey(groupId);
        String key_2 =buildUserKey(memberId);
        groupDBService.removeMemberDB(memberId,groupId);
        if(redisTemplate.hasKey(key_1)) {
            redisTemplate.opsForSet().remove(key_1, String.valueOf(memberId));
        }
        if(redisTemplate.hasKey(key_2)){
            redisTemplate.opsForSet().remove(key_2,String.valueOf(groupId));
        }
    }

    //解散群组
    public void disbandGroup(Long groupId,Long user_id){
        if(isNotOwner(groupId,user_id)){
            return;//不是群主，无权限
        }
        invalidateCache(groupId);
        String key = buildGroupKey(groupId);
        List<Long> members = getMembers(groupId);
        for(Long member: members){
            String userKey = buildUserKey(member);
            if(redisTemplate.hasKey(userKey)){
                redisTemplate.opsForSet().remove(userKey,String.valueOf(groupId));
            }
        }
        groupDBService.disbandGroupDB(groupId);
        if(redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
        }

    }



    //更改群组信息
    public boolean updateGroupName(String newName,Long group_id,Long user_id){
        if(isNotOwner(group_id,user_id)){
            return false;//不是群主，无权限
        }

        groupRepository.updateName(group_id,newName);
        invalidateCache(group_id);
        return true;
    }

    public boolean updateGroupSignature(String newSignature,Long group_id,Long user_id){
        if(isNotOwner(group_id,user_id)){
            return false;//不是群主，无权限
        }
        groupRepository.updateSignature(group_id,newSignature);
        invalidateCache(group_id);
        return true;
    }

    public boolean updateGroupAvatar(String newAvatar,Long group_id,Long user_id){
        if(isNotOwner(group_id,user_id)){
            return false;//不是群主，无权限
        }
        groupRepository.updateAvatar(group_id,newAvatar);
        invalidateCache(group_id);
        return true;
    }

    //返回群组成员的信息
    public List<UserInfo> getMembersInfo(Long groupId){
        List<Long>members = getMembers(groupId);
        if(members == null) return null;
        return userService.batchGetUserInfo(members);
    }


    //获取用户所在群组
    public List<GroupShortInfo>getGroupList(Long userId){
        List<GroupShortInfo>groupShortInfos = new ArrayList<>();
        Set<String> ids = messageCacheService.getGroupIds(userId);
        for(String id : ids){
            GroupShortInfo shortInfo = groupSearchService.getGroupShortInfo(Long.valueOf(id));
            groupShortInfos.add(shortInfo);
        }
        return groupShortInfos;
    }


    /*
    public boolean isGroupMember(Long groupId,Long userId){
        String key = buildKey(groupId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key,String.valueOf(userId));
        if(Boolean.TRUE.equals(isMember)){
            return true;
        }
        boolean inDB = relationsRepository.isGroupMember(groupId,userId);
        if(inDB){
            redisTemplate.opsForSet().add(key,String.valueOf(userId));
            refreshExpire(key);
        }
        return inDB;
    }
    */



}
