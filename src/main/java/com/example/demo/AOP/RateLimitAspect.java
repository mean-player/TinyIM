package com.example.demo.AOP;

import com.example.demo.Service.RateLimitService;
import com.example.demo.Tools.AuthUtil;
import com.example.demo.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private static final int STATUS_UNAUTHORIZED = 401;
    private static final int STATUS_TOO_MANY_REQUESTS = 429;

    private final RateLimitService rateLimitService;

    @Around("@annotation(limitAvatar) || @annotation(limitNickname) || @annotation(limitCreateGroup) ||" +
            "@annotation(limitSignature) || @annotation(limitApplication) || @annotation(limitMessageSend) ||" +
            "@annotation(limitUpload) || @annotation(limitPWReset)")
    public Object checkLimit(ProceedingJoinPoint joinPoint,
                             LimitAvatar limitAvatar,
                             LimitNickname limitNickname,
                             LimitCreateGroup limitCreateGroup,
                             LimitSignature limitSignature,
                             LimitApplication limitApplication,
                             LimitMessageSend limitMessageSend,
                             LimitUpload limitUpload,
                             LimitPWReset limitPWReset) throws Throwable {

        String userId = AuthUtil.getUserId();
        if (userId == null) {
            throw new BusinessException(STATUS_UNAUTHORIZED, "未认证的用户");
        }

        if (limitAvatar != null) {
            if (!rateLimitService.canChangeAvatar(userId)) {
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "今日头像修改次数已用完");
            }
        }
        if (limitNickname != null) {
            if (!rateLimitService.canChangeNickname(userId)) {
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "昵称修改过于频繁");
            }
        }
        if (limitSignature != null) {
            if (!rateLimitService.canChangeSignature(userId)) {
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "签名修改过于频繁");
            }
        }
        if (limitApplication != null) {
            if (!rateLimitService.canSendApplication(userId)) {
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "发送申请过于频繁");
            }
        }
        if (limitCreateGroup != null) {
            if (!rateLimitService.canCreateGroup(userId)) {
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "创建群组过于频繁");
            }
        }
        if (limitMessageSend != null) {
            if (!rateLimitService.canSendMessage(userId)) {
                log.warn("RateLimit sendMessage exceeded userId={}", userId);
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "发消息过于频繁");
            }
        }
        if (limitUpload != null) {
            if (!rateLimitService.canUpload(userId)) {
                log.warn("RateLimit upload exceeded userId={}", userId);
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "上传文件过于频繁");
            }
        }
        if (limitPWReset != null) {
            if (!rateLimitService.canResetPassword(userId)) {
                log.warn("RateLimit password reset exceeded userId={}", userId);
                throw new BusinessException(STATUS_TOO_MANY_REQUESTS, "修改密码过于频繁");
            }
        }

        return joinPoint.proceed();
    }
}
