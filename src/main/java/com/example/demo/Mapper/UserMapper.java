package com.example.demo.Mapper;

import com.example.demo.Entity.User;
import com.example.demo.Provider.UserProvider;
import com.example.demo.VO.UserInfo;
import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface UserMapper {

    // ---- Basic ----
    @SelectProvider(type = UserProvider.class, method = "selectById")
    User selectById(@Param("id") Long id);

    @InsertProvider(type = UserProvider.class, method = "insertUser")
    int insertUser(@Param("user") User user);

    @SelectProvider(type = UserProvider.class, method = "selectIdByAccount")
    Long selectIdByAccount(@Param("account") String account);

    @SelectProvider(type = UserProvider.class, method = "existByEmail")
    boolean existByEmail(@Param("emailAddr") String emailAddr);

    @SelectProvider(type = UserProvider.class, method = "selectByAccount")
    User selectByAccount(@Param("account") String account);

    @SelectProvider(type = UserProvider.class, method = "selectByEmailAddr")
    User selectByEmailAddr(@Param("emailAddr") String emailAddr);

    // ---- View ----
    @SelectProvider(type = UserProvider.class, method = "selectByIdList")
    List<UserInfo> selectByIdList(@Param("ids") List<Long> ids);

    @SelectProvider(type = UserProvider.class, method = "selectInfoById")
    UserInfo selectInfoById(@Param("id") Long id);

    // ---- Update ----
    @UpdateProvider(type = UserProvider.class, method = "updateAvatar")
    void updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);

    @UpdateProvider(type = UserProvider.class, method = "updateSignature")
    void updateSignature(@Param("userId") Long userId, @Param("signature") String signature);

    @UpdateProvider(type = UserProvider.class, method = "updateName")
    void updateName(@Param("userId") Long userId, @Param("name") String name);

    @UpdateProvider(type = UserProvider.class, method = "updatePassword")
    void updatePassword(@Param("userId") Long userId, @Param("password") String password);

    @SelectProvider(type = UserProvider.class, method = "selectEmailById")
    String selectEmailById(@Param("id") Long id);
}
