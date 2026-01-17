package com.example.demo.Controller;


import com.example.demo.AOP.*;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.UserSearch;
import com.example.demo.Entity.Storage;
import com.example.demo.Service.UserService;
import com.example.demo.Tools.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private Long getUserId(){
        return Long.valueOf(AuthUtil.getUserId());
    }

    private Boolean isValidAccount(String account) {
        if (account == null) return null;
        return account.matches("u\\d{10}");
    }



    @Operation(summary = "",description = "")
    @PostMapping("/changeAvatar")
    @LimitAvatar
    public ApiResponse<String>changeAvatar(@RequestParam("avatar")String avatar){
        userService.changeAvatar(getUserId(),avatar);
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @PostMapping("/changeNickname")
    @LimitNickname
    public ApiResponse<String>changeNickname(@RequestParam("name")String name){
        userService.changeName(getUserId(),name);
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @PostMapping("/changeSignature")
    @LimitSignature
    public ApiResponse<String>changeSignature(@RequestParam("signature")String signature){
        userService.changeSignature(getUserId(),signature);
        return ApiResponse.success();
    }

    @Operation(summary = "",description = "")
    @PostMapping("/search")
    public ApiResponse<UserSearch>searchUser(@RequestParam("account")String account){
        if(!isValidAccount(account)){
            log.error("账号不合法！");
            return ApiResponse.fail("账号不合法！");
        }
        log.info("尝试查询账号为{}的用户",account);
        UserSearch userSearch = userService.searchByAccount(account,getUserId());
        if(userSearch == null){
            return ApiResponse.fail("该用户不存在");
        }
        log.info("查询到用户id {}",userSearch.getUserInfo().getId());
        return ApiResponse.success(userSearch);
    }

    @GetMapping("/storage")
    public ApiResponse<List<Storage>>getStorage(@RequestParam("type")String type,//txt,image,audio,video,others,all
                                                @RequestParam("limit")Integer limit,
                                                @RequestParam("offset")Integer offset){
        List<Storage> storageList = userService.getStorage(getUserId(),type,limit,offset);
        return ApiResponse.success(storageList);
    }











}
