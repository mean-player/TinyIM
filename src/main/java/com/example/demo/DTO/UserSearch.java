package com.example.demo.DTO;

import com.example.demo.VO.UserInfo;
import lombok.Data;

@Data
public class UserSearch {
    private UserInfo userInfo;
    private boolean isFriend;
}
