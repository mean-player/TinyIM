package com.example.demo.Component;

import com.example.demo.Config.RabbitConfig;
import com.example.demo.Entity.Message;
import com.example.demo.Entity.Storage;
import com.example.demo.Repository.MessageRepository;
import com.example.demo.Repository.StorageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class DBWorker {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private StorageRepository storageRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskExecutor taskExecutor;

    // ----------- 配置参数 ------------
    private final int BATCH_SIZE = 50;           // 阈值
    private final long MAX_WAIT_MS = 100;        // 超时写入，ms
    private final Object lock = new Object();    // buffer 锁

    private final List<Message> buffer = new ArrayList<>();
    private Instant lastFlushTime = Instant.now();

    // ----------- 定时器检查 buffer 超时 ------------
    @PostConstruct
    public void startFlushTimer() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20); // 每 20ms 检查一次
                    flushIfTimeout();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "BufferFlushTimer");
        t.setDaemon(true);
        t.start();
    }

    private void flushIfTimeout() {
        synchronized (lock) {
            if (buffer.isEmpty()) return;
            long elapsed = Instant.now().toEpochMilli() - lastFlushTime.toEpochMilli();
            if (elapsed >= MAX_WAIT_MS) {
                List<Message> toWrite = new ArrayList<>(buffer);
                buffer.clear();
                lastFlushTime = Instant.now();
                asyncInsertMessages(toWrite, null, 0); // null channel 表示超时 flush，不 ack
            }
        }
    }

    // ----------- 异步批量写 DB ------------
    private void asyncInsertMessages(List<Message> messages, Channel channel, long tag) {
        CompletableFuture.runAsync(() -> {
            try {
                messageRepository.insertBatch(messages); // 批量写 DB
                log.info("Batch insert {} messages", messages.size());

                // 如果 Listener 传了 channel → ack
                if (channel != null) {
                    try { channel.basicAck(tag, false); } catch (IOException e) { log.error("Failed to ack", e); }
                }
            } catch (Exception e) {
                log.error("Batch insert failed", e);
                if (channel != null) {
                    try { channel.basicNack(tag, false, true); } catch (IOException ex) { log.error("Failed to nack", ex); }
                }
            }
        }, taskExecutor);
    }


    // 异步写 Storage
    private void asyncInsertStorage(Storage storage, Channel channel, long tag) {
        CompletableFuture.runAsync(() -> {
            try {
                storageRepository.insertStorage(storage);
                channel.basicAck(tag, false);
                log.info("Storage saved async: {}", storage.getId());
            } catch (Exception e) {
                log.error("Failed to insert storage async: {}", storage, e);
                try {
                    channel.basicNack(tag, false, true);
                } catch (IOException ex) {
                    log.error("Failed to nack storage: {}", storage, ex);
                }
            }
        }, taskExecutor);
    }

    // ----------- Listener 收到消息 ------------
    @RabbitListener(queues = RabbitConfig.DB_MESSAGE_QUEUE)
    public void handleMessage(String json, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Message msg = objectMapper.readValue(json, Message.class);

            synchronized (lock) {
                buffer.add(msg);

                if (buffer.size() >= BATCH_SIZE) {
                    List<Message> toWrite = new ArrayList<>(buffer);
                    buffer.clear();
                    lastFlushTime = Instant.now();
                    asyncInsertMessages(toWrite, channel, tag);
                    return; // 阈值触发异步批量写入
                }
            }

            // 阈值没到，先 ack 保证消息不重发
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Failed to deserialize message: {}", json, e);
            try { channel.basicNack(tag, false, true); } catch (IOException ex) { log.error("Failed to nack", ex); }
        }
    }


    @RabbitListener(queues = RabbitConfig.DB_STORAGE_QUEUE)
    public void handleStorage(String json, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Storage storage = objectMapper.readValue(json, Storage.class);
            asyncInsertStorage(storage, channel, tag);
        } catch (Exception e) {
            log.error("Failed to deserialize storage: {}", json, e);
            try {
                channel.basicNack(tag, false, true);
            } catch (IOException ex) {
                log.error("Failed to nack storage", ex);
            }
        }
    }
}
