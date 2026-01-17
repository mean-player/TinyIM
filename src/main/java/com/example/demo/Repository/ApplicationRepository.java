package com.example.demo.Repository;

import com.example.demo.Entity.Application;
import com.example.demo.Mapper.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationRepository {

    private final ApplicationMapper applicationMapper;

    public void addApplication(Application application){
        applicationMapper.addApplication(application);
    }

    public Application selectById(Long id){
        return applicationMapper.selectById(id);
    }

    public Application selectByFT(Long from_id,Long to_id){
        return applicationMapper.selectByFT(from_id,to_id);
    }

    public void updateStatus(Long id,int status){
        applicationMapper.updateStatus(id,status);
    }

    public void updateRead(Long id,Boolean is_read){
        applicationMapper.updateRead(id,is_read);
    }

    public List<Application> selectSentApplication(Long from_id){
        return applicationMapper.selectSentApplication(from_id);
    }

    public List<Application> selectReceiveApplication(Long to_id){
        return applicationMapper.selectReceiveApplication(to_id);
    }
}
