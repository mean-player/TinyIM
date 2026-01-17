package com.example.demo.Controller;

import com.example.demo.DTO.*;
import com.example.demo.Service.RTCService;
import com.example.demo.Service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/rtc")
public class RTCController {

    private final RTCService rtcService;
    private final TokenService tokenService;
    private String getUserId(String senderName){
        Long userId = tokenService.getUserIdByUUID(senderName);
        return String.valueOf(userId);
    }


    @Operation(summary = "",description = "")
    @GetMapping("/turnToken")
    public ApiResponse<IceServer> getTurnToken(){
        IceServer iceServer = rtcService.getTurnToken();
        if(iceServer != null){
            log.info("successfully get turnToken {} ",iceServer.getUrls());
            return ApiResponse.success(iceServer);
        }else {
            log.error("用户获取turnToken失败");
            return ApiResponse.fail("ERROR");
        }
    }

    @MessageMapping("/callRequest")
    public void callSomebody(@Payload CallRequest callRequest, Principal sender){
        callRequest.setFrom(getUserId(sender.getName()));
        rtcService.sendCallRequest(callRequest);

    }

    @MessageMapping("/callResponse")
    public void sendCallResponse(@Payload CallResponse callResponse, Principal sender){
        if(!callResponse.getType().equals("approve") && !callResponse.getType().equals("reject")){
            return;
        }
        callResponse.setFrom(getUserId(sender.getName()));
        rtcService.sendCallResponse(callResponse);

    }


    @MessageMapping("/sendIceCandidateMessage")
    public void sendIceCandidate(@Payload CandidateMessage candidateMessage, Principal sender){
        candidateMessage.setFrom(getUserId(sender.getName()));
        rtcService.candidateMessageSend(candidateMessage);

    }

    @MessageMapping("/sendSignalMessage")
    public void sendSignalMessage(@Payload SignalMessage signalMessage, Principal sender){
        signalMessage.setFrom(getUserId(sender.getName()));
        rtcService.signalMessageSend(signalMessage);
    }





}
