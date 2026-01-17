package com.example.demo.Repository;


import com.example.demo.VO.UserInfo;
import com.example.demo.Entity.User;
import com.example.demo.Mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserMapper userMapper;

    public User selectById(Long id){
        return userMapper.selectById(id);
    }

    public int addAUser(User user){
        return userMapper.insertUser(user);
    }

    public Long selectIdByAccount(String account){
        return userMapper.selectIdByAccount(account);
    }

    public boolean existByEmail(String emailAddr){
        return userMapper.existByEmail(emailAddr);
    }

    public User selectByAccount(String account){
        return userMapper.selectByAccount(account);
    }

    public User selectByEmailAddr(String emailAddr){
        return userMapper.selectByEmailAddr(emailAddr);
    }

    //View Model
    public List<UserInfo> selectByIdList(List<Long>Ids){
        return userMapper.selectByIdList(Ids);
    }
    public UserInfo selectInfoById(Long userId){
        return userMapper.selectInfoById(userId);
    }

    public void updateAvatar(Long userId,String newAvatar){
        userMapper.updateAvatar(userId,newAvatar);
    }

    public void updateSignature(Long userId,String signature){
        userMapper.updateSignature(userId,signature);
    }

    public void updateName(Long userId,String newName){
        userMapper.updateName(userId,newName);
    }

    public void updatePassword(Long userId,String newPassword){
        userMapper.updatePassword(userId,newPassword);
    }

    public String selectEmailById(Long userId){
        return userMapper.selectEmailById(userId);
    }
}
