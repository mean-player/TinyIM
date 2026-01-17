package com.example.demo.Service;

import com.example.demo.Entity.Application;
import com.example.demo.Repository.ApplicationRepository;
import com.example.demo.Repository.GroupRepository;
import com.example.demo.Component.SnowflakeIdUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private SnowflakeIdUtils idGenerator;

    @Mock
    private MessageCacheService messageCacheService;

    @Mock
    private GroupService groupService;

    @Mock
    private FriendService friendService;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void readApplicationIgnoresWhenNotFound() {
        Long userId = 1L;
        Long applicationId = 10L;
        when(applicationRepository.selectById(applicationId)).thenReturn(null);

        applicationService.readApplication(userId, applicationId);

        verify(applicationRepository, never()).updateRead(any(), any());
    }

    @Test
    void readApplicationUpdatesWhenToUserMatches() {
        Long userId = 1L;
        Long applicationId = 10L;
        Application application = new Application(applicationId, 2L, userId, 0, false, "FRIEND",
                System.currentTimeMillis(), "desc");
        when(applicationRepository.selectById(applicationId)).thenReturn(application);

        applicationService.readApplication(userId, applicationId);

        verify(applicationRepository).updateRead(applicationId, true);
    }

    @Test
    void respApplicationIgnoresWhenNotFound() {
        Long userId = 1L;
        Long applicationId = 10L;
        when(applicationRepository.selectById(applicationId)).thenReturn(null);

        applicationService.respApplication(applicationId, userId, "APPROVE");

        verify(applicationRepository, never()).updateStatus(any(), any());
    }

    @Test
    void respApplicationIgnoresWhenToUserNotMatch() {
        Long userId = 1L;
        Long applicationId = 10L;
        Application application = new Application(applicationId, 2L, 3L, 0, false, "FRIEND",
                System.currentTimeMillis(), "desc");
        when(applicationRepository.selectById(applicationId)).thenReturn(application);

        applicationService.respApplication(applicationId, userId, "APPROVE");

        verify(applicationRepository, never()).updateStatus(any(), any());
    }

    @Test
    void respApplicationApproveFriendWithinLimit() {
        Long userId = 1L;
        Long applicationId = 10L;
        long now = System.currentTimeMillis();
        Application application = new Application(applicationId, 2L, userId, 0, false, "FRIEND",
                now, "desc");
        when(applicationRepository.selectById(applicationId)).thenReturn(application);

        applicationService.respApplication(applicationId, userId, "APPROVE");

        verify(applicationRepository).updateStatus(applicationId, 1);
        verify(friendService).addFriend(eq(application.getTo_id()), eq(application.getFrom_id()));
    }
}

