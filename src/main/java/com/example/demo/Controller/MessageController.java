package com.example.demo.Controller;


import com.example.demo.DTO.*;
import com.example.demo.Entity.Message;
import com.example.demo.Service.MessageService;
import com.example.demo.Service.TokenService;
import com.example.demo.Tools.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final TokenService tokenService;

    @MessageMapping("/sendPersonalMessage")
    @SendToUser("/queue/ack")
    public AckMessage sendPersonalMessage(@Payload StorageReq storageReq,Principal sender){
        String uuid = sender.getName();
        Long userId = tokenService.getUserIdByUUID(uuid);
        if(messageService.personalMessageSend(storageReq,userId)){
            return new AckMessage(storageReq.getUuid(),true);
        }
        return new AckMessage(storageReq.getUuid(),false);

    }

    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload MessageSendReq messageSendReq, Principal sender){
        String uuid = sender.getName();
        Long userId = tokenService.getUserIdByUUID(uuid);
        log.info("uuid为{}，userid为{}的用户尝试发送消息",uuid,userId);
        messageSendReq.setSender_id(String.valueOf(userId));

        messageService.messageSend(messageSendReq);
    }

    @Operation(summary = "",description = "")
    @GetMapping("/getMaxSeq")
    public ApiResponse<List<SeqResp>> getMaxSeq(){
        String userId = AuthUtil.getUserId();
        List<SeqResp>seqRespList = messageService.getMaxSeq(Long.valueOf(userId));
        return ApiResponse.success(seqRespList);

    }

    @Operation(summary = "",description = "")
    @GetMapping("/getMessages")
    public ApiResponse<List<Message>> getMessages(@RequestParam("minSeq")Long minSeq,
                                                  @RequestParam("maxSeq")Long maxSeq,
                                                  @RequestParam("conversation_id")String conversation_id,
                                                  @RequestParam("relation_type")String relation_type){
        String userId = AuthUtil.getUserId();
        log.info("{}尝试得到消息",userId);
        log.info("seq:   {}-{}",minSeq,maxSeq);
        List<Message>messages = messageService.getMessagesBySeq(minSeq,maxSeq,conversation_id,Long.valueOf(userId),relation_type);
        if(messages.isEmpty()){
            return ApiResponse.fail("cant get messages");
        }
        return ApiResponse.success(messages);

    }


    @MessageMapping("/sendAckReadInfo")
    public void sendAckReadInfo(@Payload AckReadInfo ackReadInfo,Principal sender){
        String uuid = sender.getName();
        Long userId = tokenService.getUserIdByUUID(uuid);
        log.info("{} 发来确认信息， 对话id是{}，newseq是 {}",userId,ackReadInfo.getConversationId(),ackReadInfo.getNewSeq());
        messageService.ack(userId,ackReadInfo.getConversationId(),ackReadInfo.getNewSeq());
    }






}
