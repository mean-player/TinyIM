package com.example.demo.Service;

import com.example.demo.Component.SnowflakeIdUtils;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.ApplicationReq;
import com.example.demo.Entity.Application;
import com.example.demo.Repository.ApplicationRepository;
import com.example.demo.Repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationRepository applicationRepository;
    private final GroupRepository groupRepository;
    private final TokenService tokenService;
    private final SnowflakeIdUtils idGenerator;
    private final MessageCacheService messageCacheService;
    private final GroupService groupService;
    private final FriendService friendService;
    private static final long SEVEN_DAYS = 7L * 24 * 60 * 60 *1000;

    private String getUUIDByUserId(Long userId){
        return tokenService.getUUIDByUserId(userId);
    }


    private void sendApplication(Application application){
        if(application.getType().equals("FRIEND")){
            String uuid = getUUIDByUserId(application.getTo_id());
            if(uuid != null) {
                messagingTemplate.convertAndSendToUser(uuid,"/queue/application",application);
            }
        }
        if(application.getType().equals("GROUP")){
            Long groupOwner = groupRepository.findOwner(application.getTo_id());
            String uuid = getUUIDByUserId(groupOwner);
            if(uuid != null) {
                messagingTemplate.convertAndSendToUser(uuid,"/queue/application",application);
            }
        }
    }

    public ApiResponse<String> operateApplication(ApplicationReq applicationReq){
        Long from_id = Long.valueOf(applicationReq.getFrom());
        Long to_id = Long.valueOf(applicationReq.getTo());
        Application application = applicationRepository.selectByFT(from_id,to_id);
        boolean hasRelations = messageCacheService.hasRelationsNoDb(from_id,to_id,applicationReq.getType());
        if(hasRelations){
            return ApiResponse.fail("无需申请");
        }
        if(application == null){//不是好友 没拉黑 未拒绝 从未申请
            log.info("第一次申请");
        }else{
            int status = application.getStatus();
            if(status == -1 ){//拒绝
                return ApiResponse.fail("对方已拒绝");
            }
            if(System.currentTimeMillis() - application.getCreate_time() <= SEVEN_DAYS){
                return ApiResponse.fail("不能在一周内重复申请");
            }
            //未读或申请中  且已过七天限制
            log.info("未读或申请中  且已过七天限制");

        }
        Application newApplication = new Application(idGenerator.nextId(), from_id,to_id,0,false,
                applicationReq.getType(),System.currentTimeMillis(),applicationReq.getApp_desc());
        applicationRepository.addApplication(newApplication);
        sendApplication(newApplication);
        return ApiResponse.success();
    }


    public void readApplication(Long userId,Long applicationId){
        Application application = applicationRepository.selectById(applicationId);
        if(application == null){
            return;
        }
        if(application.getTo_id().equals(userId)){
            applicationRepository.updateRead(applicationId,true);
        }
    }

    public void respApplication(Long applicationId,Long userId,String result){
        Application application = applicationRepository.selectById(applicationId);
        if(application == null){
            return;
        }
        if(!application.getTo_id().equals(userId)){
            return;
        }
        if(application.getStatus() == 0 && (System.currentTimeMillis() - application.getCreate_time() <= SEVEN_DAYS)){
            if(result.equals("APPROVE")) {
                applicationRepository.updateStatus(applicationId,1);
                if(application.getType().equals("FRIEND")) friendService.addFriend(application.getTo_id(),application.getFrom_id());
                if(application.getType().equals("GROUP")) groupService.addMembersToGroup(application.getTo_id(),application.getFrom_id());

            }
            if(result.equals("REJECT")) applicationRepository.updateStatus(applicationId,-1);

        }
    }

    public List<Application> getSentApplication(Long userId){
        return applicationRepository.selectSentApplication(userId);
    }

    public List<Application>getReceiveApplication(Long userId){
        return applicationRepository.selectReceiveApplication(userId);
    }




}
