package com.example.demo.Component;


import com.example.demo.Service.ReadRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReadRecordFlushScheduler {

    private final StringRedisTemplate redisTemplate;
    private final ReadRecordService readRecordService;

    private static final String ONLINE_PREFIX = "user:online:";

    /**
     * 每3秒扫描在线用户并刷脏数据到DB
     */
    @Scheduled(fixedDelay = 3000)
    public void scheduledFlush() {
        try {
            scanOnlineAndFlush();
        } catch (Exception e) {
            log.error("scheduledFlush error", e);
        }
    }

    private void scanOnlineAndFlush() {
        Cursor<byte[]> cursor = null;
        int flushCount = 0;

        try {
            cursor = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(ScanOptions.scanOptions()
                            .match(ONLINE_PREFIX + "*")
                            .count(200)
                            .build());

            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Long userId = parseUserId(key);
                if (userId == null) {
                    continue;
                }

                try {
                    readRecordService.flushReadRecordToDB(userId);
                    flushCount++;
                } catch (Exception e) {
                    log.error("flush userId={} failed", userId, e);
                }
            }

        } finally {
            if (cursor != null) try { cursor.close(); } catch (Exception ignore) {}
        }

        if (flushCount > 0) {
            log.info("定时刷新已读记录, 在线用户数={} flushUsers={}", flushCount, flushCount);
        }
    }

    private Long parseUserId(String key) {
        if (!key.startsWith(ONLINE_PREFIX)) return null;
        try {
            return Long.valueOf(key.substring(ONLINE_PREFIX.length()));
        } catch (Exception e) {
            return null;
        }
    }
}
