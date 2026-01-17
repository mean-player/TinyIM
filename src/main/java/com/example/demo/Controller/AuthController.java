package com.example.demo.Controller;



import com.example.demo.AOP.LimitPWReset;
import com.example.demo.Component.JwtService;
import com.example.demo.DTO.ApiResponse;
import com.example.demo.DTO.LocalSeq;
import com.example.demo.DTO.LoginRequest;

import com.example.demo.DTO.TokenResp;
import com.example.demo.Service.LoginService;
import com.example.demo.Service.RegisterService;
import com.example.demo.Service.TokenService;
import com.example.demo.Tools.AuthUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("/auth")
public class AuthController {


    private final LoginService loginService;
    private final RegisterService registerService;
    private final JwtService jwtService;
    private final TokenService tokenService;


    @PostMapping("/refresh")
    public ApiResponse<String> refresh(@RequestParam("refreshToken")String refreshToken) {


        try {

            Map<Object,Object> userMap = tokenService.getRefreshToken(refreshToken);
            if (userMap == null) {
                return ApiResponse.invalid("Invalid refresh token");
            }

            String userId = userMap.get("userId").toString();
            String account = userMap.get("account").toString();
            log.info("{}   {}",userId,account);
            // 生成新的 access token
            String newAccessToken = jwtService.generateAccessToken(Long.valueOf(userId),account);

            return ApiResponse.success(newAccessToken);
        } catch (ExpiredJwtException e) {
            return ApiResponse.invalid("Refresh token expired, please login again");
        }
    }


    @PostMapping("/beforeLoginByEmail")
    public ApiResponse<String> beforeLoginByEmail(@RequestParam("email_addr")String email_addr){
        String uuid = loginService.beforeLoginByEmail(email_addr);
        return ApiResponse.success(uuid);

    }

    @Operation(summary = "",description = "")
    @PostMapping("/login")
    public ApiResponse<TokenResp> login(@RequestBody LoginRequest req) {
        TokenResp tokenResp = loginService.login(req);

        if ( tokenResp!=null) {
            log.info("登录成功！ token：{}",tokenResp);
            return ApiResponse.success(tokenResp);
        } else {
            return ApiResponse.fail("Invalid credentials");
        }
    }

    @Operation(summary = "",description = "")
    @PostMapping("/beforeRegister")
    public ApiResponse<String> beforeRegister(@RequestParam("emailAddr")String emailAddr){
        log.info("email地址为 ：{}的用户尝试注册账号",emailAddr);
        String result = registerService.sendEmail(emailAddr);
        if(result == null || result.isEmpty()){
            log.error("未成功返回uuid");
            return ApiResponse.fail("please try later");
        }else{
            log.info("成功返回uuid，uuid为{}",result);
            return ApiResponse.success(result);
        }
    }

    @Operation(summary = "",description = "")
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestParam("uuid")String uuid,
                                   @RequestParam("code")String code,
                                   @RequestParam("emailAddr")String emailAddr,
                                   @RequestParam("name")
                                   @Size(max=10,message = "昵称不能超过十个字符")
                                   @NotBlank(message = "昵称不能为空")
                                            String name,
                                   @RequestParam("password")@NotBlank(message = "密码不能为空")
                                            String password
                                   ){

        log.info("email地址为{}的用户正在注册",emailAddr);
        return registerService.register(uuid, code, emailAddr, name, password);// 返回 账户account
    }

    @Operation(summary = "",description = "")
    @GetMapping("/localSeq")
    public ApiResponse<List<LocalSeq>> getLocalSeq(){
        String userId = AuthUtil.getUserId();
        List<LocalSeq>localSeqList = loginService.getLocalSeq(Long.valueOf(userId));
        return ApiResponse.success(localSeqList);
    }

    //修改密码
    @Operation(summary = "",description = "")
    @PostMapping("/beforeResetPassword")
    @LimitPWReset
    public ApiResponse<?> beforeResetPassword(){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        String uuid = registerService.beforeResetPassword(userId);
        if(uuid == null){
            return ApiResponse.fail("请稍后再试");
        }
        return ApiResponse.success(uuid);
    }

    @Operation(summary = "",description = "")
    @PostMapping("/resetPassword")
    public ApiResponse<?>resetPassword(@RequestParam("uuid")String uuid,
                                       @RequestParam("code")String code,
                                       @RequestParam("newPassword")String newPassword){
        Long userId = Long.valueOf(AuthUtil.getUserId());
        return registerService.resetPassword(uuid,code,userId,newPassword);
    }



}