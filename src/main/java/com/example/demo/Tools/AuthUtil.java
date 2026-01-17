package com.example.demo.Tools;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static String getUserId(){
        var context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return null;
        }
        Object principal = context.getAuthentication().getPrincipal();
        return principal == null ? null : (String) principal;
    }
}
