package com.example.demo.Config;


import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 交换机名称
    public static final String DB_EXCHANGE = "db_exchange";

    // 队列名称
    public static final String DB_MESSAGE_QUEUE = "db_message_queue";
    public static final String DB_STORAGE_QUEUE = "db_storage_queue";


    // 路由键
    public static final String DB_MESSAGE_KEY = "db.message";
    public static final String DB_STORAGE_KEY = "db.storage";


    //定义 Direct Exchange

    @Bean
    public DirectExchange dbExchange() {
        return new DirectExchange(DB_EXCHANGE, true, false);
    }


     //队列

    @Bean
    public Queue dbMessageQueue() {
        return QueueBuilder.durable(DB_MESSAGE_QUEUE)
                .build();
    }

    @Bean
    public Queue dbStorageQueue() {
        return QueueBuilder.durable(DB_STORAGE_QUEUE)
                .build();
    }



    //队列绑定

    @Bean
    public Binding meaasgeBinding(DirectExchange dbExchange, Queue dbMessageQueue) {
        return BindingBuilder.bind(dbMessageQueue).to(dbExchange).with(DB_MESSAGE_KEY);
    }

    @Bean
    public Binding storageBinding(DirectExchange dbExchange, Queue dbStorageQueue) {
        return BindingBuilder.bind(dbStorageQueue).to(dbExchange).with(DB_STORAGE_KEY);
    }



}