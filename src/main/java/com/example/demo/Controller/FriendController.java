package com.example.demo.Controller;

import com.example.demo.DTO.ApiResponse;
import com.example.demo.Service.FriendService;
import com.example.demo.Tools.AuthUtil;
import com.example.demo.VO.UserInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;


    @Operation(summary = "",description = "")
    @PostMapping("/remove")
    public ApiResponse<String> removeFriend(@RequestParam("friend_id")String friend_id){
        Long user_id = Long.valueOf(AuthUtil.getUserId());
        friendService.removeFriend(user_id,Long.valueOf(friend_id));
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @GetMapping("/friendList")
    public ApiResponse<List<UserInfo>>getFriendList(){
        Long user_id = Long.valueOf(AuthUtil.getUserId());
        return ApiResponse.success(friendService.getFriendList(user_id));

    }

}
