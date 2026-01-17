package com.example.demo.Component;

import com.example.demo.Config.RabbitConfig;
import com.example.demo.Entity.Message;
import com.example.demo.Entity.Storage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DBMProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    //发送message
    public void sendDBMessage(Message message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.DB_EXCHANGE,
                    RabbitConfig.DB_MESSAGE_KEY,
                    json
            );
        }catch (JsonProcessingException e){
            log.error("sendDBMessage(Message) serialization failed: {}", e.getMessage());
        }
    }

    //发送storage
    public void sendDBMessage(Storage storage) {
        try {
            String json = objectMapper.writeValueAsString(storage);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.DB_EXCHANGE,
                    RabbitConfig.DB_STORAGE_KEY,
                    json
            );
        }catch (JsonProcessingException e){
            log.error("sendDBMessage(Storage) serialization failed: {}", e.getMessage());
        }
    }

}
