package com.example.demo.Service;


import com.example.demo.Component.TurnTokenUtil;
import com.example.demo.DTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class RTCService {

    private final TurnTokenUtil turnTokenUtil;

    private final TokenService tokenService;
    private final MessageCacheService messageCacheService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    private static final Duration CALL_REQUEST_TTL = Duration.ofSeconds(90);

    // 枚举管理 call 状态
    public enum CallStatus {
        request, approve, reject
    }

    private String buildKey(Long callerId, Long calleeId) {
        return callerId + "CALL" + calleeId;
    }


    public IceServer getTurnToken(){
        return turnTokenUtil.generateCredential();
    }

    private String getUUIDByUserId(Long userId){
        return tokenService.getUUIDByUserId(userId);
    }

    private Boolean getCallPermission(Long from_id,Long to_id){
        String key_1 = buildKey(from_id,to_id);
        String key_2 = buildKey(to_id,from_id);
        String value_1 = redisTemplate.opsForValue().get(key_1);
        String value_2 = redisTemplate.opsForValue().get(key_2);
        Boolean result_1 = (value_1 != null && value_1.equals(CallStatus.approve.name()));
        Boolean result_2 = (value_2 != null && value_2.equals(CallStatus.approve.name()));
        return (result_1 || result_2);
    }

    // 通用检查方法
    private boolean checkSendPermission(Long fromId, Long toId) {
        if (!messageCacheService.hasRelations(fromId, toId, "FRIEND")) {
            log.warn("用户 {} 与 {} 非好友，拒绝发送", fromId, toId);
            return false;
        }
        if (redisTemplate.opsForValue().get("user:online:" + toId) == null) {
            log.warn("接收方 {} 不在线", toId);
            return false;
        }
        if (!getCallPermission(fromId, toId)) {
            log.warn("通话未批准，拒绝发送消息 {} -> {}", fromId, toId);
            return false;
        }
        return true;
    }

    public void sendCallRequest(CallRequest callRequest){
        Long from_id = Long.valueOf(callRequest.getFrom());
        Long to_id = Long.valueOf(callRequest.getTo());
        if(!messageCacheService.hasRelations(from_id,to_id,"FRIEND")){
            log.warn("无权限");
            return;
        }
        String key = buildKey(from_id,to_id);
        String value = redisTemplate.opsForValue().get(key);
        if(value != null ){
            log.warn("不能重复发送请求");
            return;
        }
        if(redisTemplate.opsForValue().get("user:online:" + to_id) == null){
            log.warn("接收方不在线");
            return;
        }
        redisTemplate.opsForValue().set(key,"request", CALL_REQUEST_TTL);
        String uuid = getUUIDByUserId(to_id);
        if(uuid!=null){
            messagingTemplate.convertAndSendToUser(uuid,"/queue/callRequest",callRequest);
        }

    }


    public void sendCallResponse(CallResponse callResponse){
        Long from_id = Long.valueOf(callResponse.getFrom());
        Long to_id = Long.valueOf(callResponse.getTo());
        if(!messageCacheService.hasRelations(from_id,to_id,"FRIEND")){
            log.warn("无权限");
            return;
        }
        String key = buildKey(to_id,from_id);
        String value = redisTemplate.opsForValue().get(key);
        if(value == null){
            log.warn("过期回复");
            return;
        }
        if(!value.equals("request")){
            log.warn("重复回复");
            return;
        }
        if(redisTemplate.opsForValue().get("user:online:" + to_id) == null){
            log.warn("接收方不在线");
            return;
        }
        redisTemplate.opsForValue().set(key,callResponse.getType(),CALL_REQUEST_TTL);
        String uuid = getUUIDByUserId(to_id);
        if(uuid!=null){
            messagingTemplate.convertAndSendToUser(uuid,"/queue/callResponse",callResponse);
        }

    }


    public void candidateMessageSend(CandidateMessage candidateMessage){
        Long from_id = Long.valueOf(candidateMessage.getFrom());
        Long to_id = Long.valueOf(candidateMessage.getTo());
        if(!checkSendPermission(from_id,to_id)){
            return;
        }
        String uuid = getUUIDByUserId(to_id);
        if(uuid!=null){
            messagingTemplate.convertAndSendToUser(uuid,"/queue/signal/candidate",
                    candidateMessage);
        }
    }

    public void signalMessageSend(SignalMessage signalMessage) {
        Long from_id = Long.valueOf(signalMessage.getFrom());
        Long to_id = Long.valueOf(signalMessage.getTo());
        if(!checkSendPermission(from_id,to_id)){
            return;
        }
        String uuid = getUUIDByUserId(to_id);
        if (uuid != null) {
            if (signalMessage.getSdp().getType().equals("offer"))
                messagingTemplate.convertAndSendToUser(uuid, "/queue/signal/offer", signalMessage);
            else
                messagingTemplate.convertAndSendToUser(uuid, "/queue/signal/answer", signalMessage);

        }
    }
}
