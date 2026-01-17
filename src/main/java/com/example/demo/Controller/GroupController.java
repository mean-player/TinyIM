package com.example.demo.Controller;


import com.example.demo.AOP.LimitCreateGroup;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.CreateGroupReq;
import com.example.demo.Service.GroupSearchService;
import com.example.demo.VO.GroupShortInfo;
import com.example.demo.Service.GroupService;
import com.example.demo.Tools.AuthUtil;
import com.example.demo.VO.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupSearchService groupSearchService;
    private Long getUserId(){
        return Long.valueOf(AuthUtil.getUserId());
    }

    @LimitCreateGroup
    @Operation(summary = "",description = "")
    @PostMapping("/create")
    public ApiResponse<String>createGroup(@RequestBody CreateGroupReq createGroupReq){
        groupService.createGroup(createGroupReq,getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @PostMapping("/leave")
    public ApiResponse<String> leaveGroup(@RequestParam("group_id")String group_id){
        if(groupService.leaveGroup(getUserId(),Long.valueOf(group_id))){
            return ApiResponse.success();
        }else{
            return ApiResponse.fail("无法退出你不在的群组");
        }
    }

    @Operation(summary = "",description = "")
    @RequestMapping("/removeMember")
    public ApiResponse<?> removeMember(@RequestParam("group_id")String group_id,
                                       @RequestParam("member_id")String member_id){
        groupService.removeMembersFromGroup(Long.valueOf(group_id),Long.valueOf(member_id),getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @PostMapping("/disband")
    public ApiResponse<String> disbandGroup(@RequestParam("group_id")String group_id){
        groupService.disbandGroup(Long.valueOf(group_id),getUserId());
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @GetMapping("/memberList")
    public ApiResponse<List<UserInfo>>getMemberList(@RequestParam("group_id")String group_id){
        List<UserInfo> userInfoList = groupService.getMembersInfo(Long.valueOf(group_id));
        if(userInfoList == null) return ApiResponse.fail("群组不存在");
        return ApiResponse.success(userInfoList);
    }

    @GetMapping("/groupInfo")
    public ApiResponse<GroupShortInfo> getGroupInfo(@RequestParam("group_id")String group_id){
        GroupShortInfo groupShortInfo = groupSearchService.getGroupShortInfo(Long.valueOf(group_id));
        if(groupShortInfo != null){
            return ApiResponse.success(groupShortInfo);
        }
        return ApiResponse.fail("获取群信息失败");


    }


    @PostMapping("/changeName")
    public ApiResponse<String> changeGroupName(@RequestParam("newName")String newName,
                                               @RequestParam("group_id")String group_id){
        if(newName == null || group_id == null){
            return ApiResponse.fail("输入信息无效！");
        }
        if(Boolean.TRUE.equals(groupService.updateGroupName(newName,Long.valueOf(group_id),getUserId()))){
            return ApiResponse.success();
        }
        return ApiResponse.fail("更新群组名称失败！");
    }

    @PostMapping("/changeSignature")
    public ApiResponse<String> changeGroupSignature(@RequestParam("newSignature")String newSignature,
                                               @RequestParam("group_id")String group_id){
        if(newSignature == null || group_id == null){
            return ApiResponse.fail("输入信息无效！");
        }
        if(Boolean.TRUE.equals(groupService.updateGroupSignature(newSignature,Long.valueOf(group_id),getUserId()))){
            return ApiResponse.success();
        }
        return ApiResponse.fail("更新群组签名失败！");

    }

    @PostMapping("/changeAvatar")
    public ApiResponse<String> changeGroupAvatar(@RequestParam("newName")String newAvatar,
                                               @RequestParam("group_id")String group_id){
        if(newAvatar == null || group_id == null){
            return ApiResponse.fail("输入信息无效！");
        }
        if(Boolean.TRUE.equals(groupService.updateGroupAvatar(newAvatar,Long.valueOf(group_id),getUserId()))){
            return ApiResponse.success();
        }
        return ApiResponse.fail("更新群组头像失败！");

    }


    @GetMapping("/groupList")
    public ApiResponse<List<GroupShortInfo>> getGroupList(){
        List<GroupShortInfo> groupShortInfos = groupService.getGroupList(getUserId());
        return ApiResponse.success(groupShortInfos);
    }



    



}
