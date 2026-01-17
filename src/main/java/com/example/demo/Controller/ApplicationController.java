package com.example.demo.Controller;

import com.example.demo.AOP.LimitApplication;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.ApplicationReq;
import com.example.demo.Entity.Application;
import com.example.demo.Service.ApplicationService;
import com.example.demo.Tools.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    @LimitApplication
    @Operation(summary = "",description = "")
    @PostMapping("/sendApplication")
    public ApiResponse<String> sendApplication(@RequestBody @Valid ApplicationReq applicationReq){

        String userId = AuthUtil.getUserId();
        if(Objects.equals(applicationReq.getTo(), Long.valueOf(userId))){
            log.warn("自己不能给自己发送申请！");
            return ApiResponse.fail("invalid");
        }

        applicationReq.setFrom(userId);

        return applicationService.operateApplication(applicationReq);

    }

    @Operation(summary = "",description = "")
    @PostMapping("/read")
    public void readApplication(@RequestParam("applicationId")String applicationId){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        applicationService.readApplication(userId,Long.valueOf(applicationId));
    }


    @Operation(summary = "",description = "")
    @PostMapping("/resp")
    public void respApplication(@RequestParam("applicationId")String applicationId,
                                               @RequestParam("result")String result){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        applicationService.respApplication(Long.valueOf(applicationId),userId,result);
    }

    @RequestMapping("/getSent")
    public ApiResponse<List<Application>>getSentApplication(){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        List<Application>applications = applicationService.getSentApplication(userId);
        return ApiResponse.success(applications);
    }

    @RequestMapping("/getReceive")
    public ApiResponse<List<Application>>getReceiveApplication(){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        List<Application>applications = applicationService.getReceiveApplication(userId);
        return ApiResponse.success(applications);
    }



}
