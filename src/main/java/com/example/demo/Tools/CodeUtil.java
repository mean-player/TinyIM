package com.example.demo.Tools;

import java.util.Random;

public class CodeUtil {
    public static String generateCode(){
        return String.valueOf((int)((Math.random()*9+1)*100000)); //6位随机验证码
    }


    public static String randomNumeric(int count) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
