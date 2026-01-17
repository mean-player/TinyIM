package com.example.demo.Service;

import com.example.demo.Component.DBMProducer;
import com.example.demo.Component.SnowflakeIdUtils;
import com.example.demo.DTO.*;
import com.example.demo.Entity.Message;
import com.example.demo.Entity.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final SimpMessagingTemplate messagingTemplate;
    private final TokenService tokenService;
    private final StringRedisTemplate redisTemplate;
    private final SnowflakeIdUtils idGenerator;
    private final MessageCacheService  messageCacheService;
    private final TimelineService timelineService;
    private final DBMProducer dbmProducer;
    private final GroupService groupService;
    private final ReadRecordService readRecordService;


    private String getUUIDByUserId(Long userId){
        return tokenService.getUUIDByUserId(userId);
    }


    private void sendAckSeq(Long seq,Long sender_id,String conversation_id,String relation_type){
        String uuid = getUUIDByUserId(sender_id);
        if (uuid != null) {
            SeqResp seqResp = new SeqResp(seq,conversation_id,relation_type);
            messagingTemplate.convertAndSendToUser(uuid, "/queue/messageSeq", seqResp);
        }
    }

    private boolean isUserInConversation(String userId, String conversationId) {
        if (userId == null || conversationId == null) {
            return false;
        }
        String[] parts = conversationId.split("T");
        for (String part : parts) {
            if (userId.equals(part)) {
                return true;
            }
        }
        return false;
    }


    @Async("taskExecutor")
    public void asyncSendSeq(String uuid,SeqResp seqResp){
        try {
            messagingTemplate.convertAndSendToUser(uuid, "/queue/messageSeq", seqResp);
        }catch (Exception e){
            log.error("asyncSendSeq ERROR {} {}",uuid,seqResp,e);
        }
    }

    private void sendSeq(Long seq,Long sender_id,Long receiver_id,String relation_type){
        if(relation_type.equals("FRIEND")) {//发送好友信息
            if(redisTemplate.opsForValue().get("user:online:" + receiver_id) == null){//接收方不在线
                return;
            }
            String conversation_id = Math.min(sender_id,receiver_id)+"T"+Math.max(sender_id,receiver_id);
            String uuid = getUUIDByUserId(receiver_id);
            if (uuid != null) {
                SeqResp seqResp = new SeqResp(seq,conversation_id,"FRIEND");
                asyncSendSeq(uuid,seqResp);
            }
        }else{//发送群组消息
            Long groupId = receiver_id;

            SeqResp seqResp = new SeqResp(seq,String.valueOf(groupId),"GROUP");
            List<Long>members = groupService.getMembers(groupId);
            for(Long m : members){
                if(m.equals(sender_id)){
                    continue;
                }
                if(redisTemplate.opsForValue().get("user:online:" + m) == null){//接收方不在线
                    continue;
                }
                String uuid = getUUIDByUserId(m);
                if (uuid != null) {
                    asyncSendSeq(uuid,seqResp);
                }
            }

        }
    }

    public List<SeqResp> getMaxSeq(Long user_id){
        List<SeqResp> result = new ArrayList<>();
        Set<String> friendIds = messageCacheService.getFriendIds(user_id);
        Set<String> groupIds = messageCacheService.getGroupIds(user_id);
        if(!friendIds.isEmpty()){
            for(String friendId : friendIds){
                String conversationId = Math.min(Long.parseLong(friendId), user_id) + "T" + Math.max(Long.parseLong(friendId), user_id);
                SeqResp seqResp = new SeqResp(timelineService.fetchMaxSeq(conversationId),conversationId,"FRIEND");
                result.add(seqResp);
            }
        }
        if(!groupIds.isEmpty()) {
            for (String groupId : groupIds) {
                String conversationId = groupId;
                SeqResp seqResp = new SeqResp(timelineService.fetchMaxSeq(conversationId), conversationId,"GROUP");
                result.add(seqResp);
            }
        }
        return result;

    }

    //消息到达后端后的操作
    public void messageSend(MessageSendReq messageSendReq){
        String relation_type = messageSendReq.getRelationType();
        Long sender_id = Long.valueOf(messageSendReq.getSender_id());
        Long receiver_id = Long.valueOf(messageSendReq.getReceiver_id());
        //没有权限发送消息
        if(! messageCacheService.hasRelations(sender_id,receiver_id,relation_type)){
            log.error("没有消息发送权限");
            return;
        }
        Long messageId = idGenerator.nextId();
        String conversationId;
        if(relation_type.equals("FRIEND")) {
            conversationId = Math.min(sender_id,receiver_id)+"T"+Math.max(sender_id,receiver_id);
        }else{
            conversationId = String.valueOf(receiver_id);
        }
        // add timeline:{conversationId}  ----->seq  :  messageId
        long seq = timelineService.addMessage(conversationId,messageId);
        log.info("发送消息 relationType={}, conversationId={}, seq={}, messageId={}", relation_type, conversationId, seq, messageId);
        //Long id,Long seq,String conversation_id,String content,String type,Long sender_id,Long receiver_id,LocalDateTime send_time
        Message message = new Message(messageId,seq,conversationId,messageSendReq.getContent(),
                messageSendReq.getType(),sender_id,receiver_id,System.currentTimeMillis());
        //cache message into redis
        messageCacheService.cacheMessage(message);
        //mq
        dbmProducer.sendDBMessage(message);
        //send seq
        sendAckSeq(seq,sender_id,conversationId,relation_type);
        sendSeq(seq,sender_id,receiver_id,relation_type);


    }


    public List<Message> getMessagesBySeq(Long minSeq, Long maxSeq,String conversation_id,Long userId,String relation_type){

        if(!(relation_type.equals("FRIEND") || relation_type.equals("GROUP"))){
            log.error("字段错误{}",relation_type);
            return List.of();
        }

        if(relation_type.equals("FRIEND") && (! isUserInConversation(String.valueOf(userId),conversation_id))) {
            log.error("身份不合法");
            return List.of();
        }

        if(relation_type.equals("GROUP") && (!messageCacheService.hasRelations(userId,Long.valueOf(conversation_id),"GROUP"))) {
            log.error("身份不合法");
            return List.of();
        }

        return timelineService.getMessages(conversation_id,minSeq,maxSeq);

    }

    public void ack(Long userId,String conversationId,Long newSeq){
        readRecordService.updateSeq(userId,conversationId,newSeq);
    }


    public boolean personalMessageSend(StorageReq storageReq,Long userId){
        log.info("{}发送了个人消息",userId);
        String uuid = getUUIDByUserId(userId);
        if(uuid == null){
            log.error("无法找到个人信息");
            return false;
        }
        Long id = idGenerator.nextId();
        Storage storage = new Storage(id,storageReq.getContent(),storageReq.getType(),userId,System.currentTimeMillis());
        dbmProducer.sendDBMessage(storage);
        messagingTemplate.convertAndSendToUser(uuid,"/queue/storage",storage);
        return true;

    }





}
