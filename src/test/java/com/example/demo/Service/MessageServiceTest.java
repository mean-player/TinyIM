package com.example.demo.Service;

import com.example.demo.DTO.StorageReq;
import com.example.demo.Entity.Message;
import com.example.demo.Component.DBMProducer;
import com.example.demo.Component.SnowflakeIdUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private TokenService tokenService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SnowflakeIdUtils idGenerator;

    @Mock
    private MessageCacheService messageCacheService;

    @Mock
    private TimelineService timelineService;

    @Mock
    private DBMProducer dbmProducer;

    @Mock
    private GroupService groupService;

    @Mock
    private ReadRecordService readRecordService;

    @InjectMocks
    private MessageService messageService;

    @Test
    void getMessagesBySeqReturnsEmptyWhenRelationTypeInvalid() {
        List<Message> result = messageService.getMessagesBySeq(1L, 10L, "1T2", 1L, "OTHER");
        assertTrue(result.isEmpty());
        verify(timelineService, never()).getMessages(anyString(), anyLong(), anyLong());
    }

    @Test
    void getMessagesBySeqReturnsEmptyWhenFriendNotInConversation() {
        List<Message> result = messageService.getMessagesBySeq(1L, 10L, "1T2", 3L, "FRIEND");
        assertTrue(result.isEmpty());
        verify(timelineService, never()).getMessages(anyString(), anyLong(), anyLong());
    }

    @Test
    void getMessagesBySeqReturnsEmptyWhenGroupRelationMissing() {
        when(messageCacheService.hasRelations(1L, 10L, "GROUP")).thenReturn(false);

        List<Message> result = messageService.getMessagesBySeq(1L, 10L, "10", 1L, "GROUP");
        assertTrue(result.isEmpty());
        verify(timelineService, never()).getMessages(anyString(), anyLong(), anyLong());
    }

    @Test
    void getMessagesBySeqReturnsMessagesWhenFriendValid() {
        Message message = new Message();
        when(timelineService.getMessages("1T2", 1L, 10L)).thenReturn(List.of(message));

        List<Message> result = messageService.getMessagesBySeq(1L, 10L, "1T2", 1L, "FRIEND");

        assertEquals(1, result.size());
        assertEquals(message, result.get(0));
    }

    @Test
    void personalMessageSendReturnsFalseWhenUuidMissing() {
        StorageReq req = new StorageReq();
        when(tokenService.getUUIDByUserId(1L)).thenReturn(null);

        boolean result = messageService.personalMessageSend(req, 1L);

        assertFalse(result);
        verify(dbmProducer, never()).sendDBMessage(any());
    }

    @Test
    void personalMessageSendReturnsTrueWhenSuccess() {
        StorageReq req = new StorageReq();
        req.setContent("c");
        req.setType("t");
        when(tokenService.getUUIDByUserId(1L)).thenReturn("uuid");
        when(idGenerator.nextId()).thenReturn(1L);

        boolean result = messageService.personalMessageSend(req, 1L);

        assertTrue(result);
        verify(dbmProducer).sendDBMessage(any());
        verify(messagingTemplate).convertAndSendToUser(eq("uuid"), eq("/queue/storage"), any());
    }
}

