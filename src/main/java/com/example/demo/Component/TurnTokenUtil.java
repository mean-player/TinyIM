package com.example.demo.Component;


import com.example.demo.DTO.IceServer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TurnTokenUtil {



    private final CoTurnProperties coTurnProperties; // static-auth-secret
    private static final long TTL = 600; // 秒，凭证有效期


    public IceServer generateCredential() {
        long timestamp = Instant.now().getEpochSecond() + TTL;
        String username = Long.toString(timestamp);

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(coTurnProperties.getSecretKey().getBytes(), "HmacSHA1"));
            byte[] rawHmac = mac.doFinal(username.getBytes());
            String password = Base64.getEncoder().encodeToString(rawHmac);

            List<String> urls = List.of(
                    "stun:"+coTurnProperties.getHost(),
                    "turn:"+coTurnProperties.getHost()+"?transport=udp",
                    "turn:"+coTurnProperties.getHost()+"?transport=tcp"
            );
            return new IceServer(urls,username, password);
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }



}