package com.example.demo.Component;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String IM_Mail;

    @Async("taskExecutor")
    public void sendEmail(String emailAddr, String info){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(IM_Mail);
        mailMessage.setTo(emailAddr);
        mailMessage.setSubject("验证码");
        mailMessage.setText(info);

        mailSender.send(mailMessage);
    }
}
