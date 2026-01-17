package com.example.demo.Service;

import com.example.demo.Entity.ReadRecord;
import com.example.demo.Repository.ReadRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadRecordService {

    private final ReadRecordRepository readRecordRepository;
    private final StringRedisTemplate redisTemplate;

    private static final int FLUSH_THRESHOLD = 10;
    private String buildKey(Long userId){
        return "readRecord:"+userId;
    }
    private String buildDirtyKey(Long userId){return "dirty:"+userId;}

    public void cacheUserReadRecords(Long userId){
        String key = buildKey(userId);
        if(redisTemplate.hasKey(key)){
           return;
        }
        List<ReadRecord> readRecords = readRecordRepository.selectReadRecords(userId);
        if(readRecords == null || readRecords.isEmpty()){
            return;
        }
        Map<String,String> map = new HashMap<>();
        for(ReadRecord readRecord : readRecords){
            map.put(readRecord.getConversation_id(),String.valueOf(readRecord.getSeq()));
        }
        redisTemplate.opsForHash().putAll(key,map);
        redisTemplate.expire(key, Duration.ofDays(7));
        log.info("缓存用户已读信息 userId={} size={}", userId, map.size());
    }


    public void flushReadRecordToDB(Long userId) {
        String key = buildKey(userId);
        String dirtyKey = buildDirtyKey(userId);

        // 取 dirty conversation 列表
        List<String> dirtyList = redisTemplate.opsForSet().members(dirtyKey)
                .stream().map(String::valueOf).toList();

        if (dirtyList.isEmpty()) {
            return;
        }

        for (String conversationId : dirtyList) {
            Object val = redisTemplate.opsForHash().get(key, conversationId);
            if (val == null) {
                // 理论不会出现，但安全保护
                redisTemplate.opsForSet().remove(dirtyKey, conversationId);
                continue;
            }
            Long seq = Long.valueOf((String) val);
            readRecordRepository.updateSeq(userId, conversationId, seq);
            log.info("updated!");

            // remove dirty
            redisTemplate.opsForSet().remove(dirtyKey, conversationId);
        }

        // cnt 刷掉（脏标清了就没意义了）
        redisTemplate.delete(key + ":cnt");

        log.info("flush 用户={} dirtyCount={}", userId, dirtyList.size());
    }


    @Async("taskExecutor")
    public void updateSeq(Long userId,String conversationId,Long newSeq){
        String key = buildKey(userId);
        String dirtyKey = buildDirtyKey(userId);
        String cntKey = key+":cnt";
        redisTemplate.opsForSet().add(dirtyKey,conversationId);

        Object oldVal = redisTemplate.opsForHash().get(key,conversationId);
        Long oldSeq = oldVal == null ? -1L : Long.valueOf((String) oldVal);
        if(newSeq<= oldSeq){
            log.error("未更新成功! {} {}",newSeq,oldSeq);
            return;
        }

        redisTemplate.opsForHash().put(key,conversationId,String.valueOf(newSeq));
        Long cnt = redisTemplate.opsForValue().increment(cntKey);
        if(cnt == 1){
            redisTemplate.expire(cntKey,Duration.ofDays(1));
        }
        if(cnt>=FLUSH_THRESHOLD){
            flushReadRecordToDB(userId);
        }
        log.debug("updateSeq userId={} conversationId={} newSeq={} cnt={}", userId, conversationId, newSeq, cnt);
    }

}
