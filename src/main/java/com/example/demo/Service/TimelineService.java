package com.example.demo.Service;



import com.example.demo.Entity.Message;
import com.example.demo.Repository.MessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {


    private final StringRedisTemplate redis;
    private final MessageRepository messageRepository;
    private final MessageCacheService messageCacheService;


    private final DefaultRedisScript<Long> script = new DefaultRedisScript<>();

    private static final String LUA =
            "local timeline = KEYS[1]\n" +
                    "local lastDbSeqKey = KEYS[2]\n" +
                    "local messageId = ARGV[1]\n" +
                    "local dbMaxSeq = tonumber(ARGV[2])\n" +
                    "local expireSeconds = tonumber(ARGV[3])\n" +
                    "local maxSize = tonumber(ARGV[4])\n" +
                    "local newSeq = nil\n" +
                    "local exists = redis.call('EXISTS', timeline)\n" +
                    "if exists == 1 then\n" +
                    "    local last = redis.call('ZREVRANGE', timeline, 0, 0, 'WITHSCORES')\n" +
                    "    local maxSeq = tonumber(last[2])\n" +
                    "    newSeq = maxSeq + 1\n" +
                    "else\n" +
                    "    local cached = redis.call('GET', lastDbSeqKey)\n" +
                    "    if cached then\n" +
                    "        newSeq = tonumber(cached) + 1\n" +
                    "    else\n" +
                    "        if dbMaxSeq >= 0 then\n" +
                    "            newSeq = dbMaxSeq + 1\n" +
                    "        else\n" +
                    "            newSeq = 0\n" +
                    "        end\n" +
                    "        redis.call('SET', lastDbSeqKey, newSeq, 'EX', expireSeconds)\n" +
                    "    end\n" +
                    "end\n" +
                    "redis.call('ZADD', timeline, newSeq, messageId)\n" +
                    "local size = redis.call('ZCARD', timeline)\n" +
                    "if size > maxSize then\n" +
                    "    redis.call('ZREMRANGEBYRANK', timeline, 0, size - maxSize - 1)\n" +
                    "end\n" +
                    "return newSeq";

    @PostConstruct
    public void initScriptText() {
        script.setScriptText(LUA);
        script.setResultType(Long.class);
    }

    /**
     * 添加消息并返回 seq
     */
    public long addMessage(String conversationId, Long messageId) {

        String timelineKey = "timeline:" + conversationId;
        String lastDbKey = "last_db_seq:" + conversationId;

        // Redis 无数据时 DB fallback
        Long maxSeq = messageRepository.selectMaxSeq(conversationId);
        if (maxSeq == null) maxSeq = -1L;

        Long seq = redis.execute(
                script,
                Arrays.asList(timelineKey, lastDbKey),
                String.valueOf(messageId),
                String.valueOf(maxSeq),
                String.valueOf(60),         // 缓存 60 秒
                String.valueOf(100000)      // timeline 最大容量
        );

        return seq;
    }


    public Long fetchMaxSeq(String conversationId) {

        String timelineKey = "timeline:" + conversationId;
        String lastDbKey = "last_db_seq:" + conversationId;


        // 1. Redis 里是否有 timeline ZSet
        Boolean exists = redis.hasKey(timelineKey);
        if (exists != null && exists) {
            // ZREVRANGE 0 0 WITHSCORES —— 最大 seq
            Set<ZSetOperations.TypedTuple<String>> set =
                    redis.opsForZSet().reverseRangeWithScores(timelineKey, 0, 0);

            if (set != null && !set.isEmpty()) {
                ZSetOperations.TypedTuple<String> tuple = set.iterator().next();
                return tuple.getScore().longValue();  // 最大的 seq
            }
        }

        // 2. 查 Redis 缓存的 last_db_seq
        String cached = redis.opsForValue().get(lastDbKey);
        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException ignored) {}
        }

        // 3. 查数据库
        Long maxSeq = messageRepository.selectMaxSeq(conversationId);
        if (maxSeq != null && maxSeq >= 0) {

            return maxSeq;
        }

        // 4. 全都没有，返回 -1
        return -1L;
    }



    //获取minSeq+1 -------maxSeq之间的消息
    public List<Message> getMessages(
            String conversationId,
            Long minSeq,
            Long maxSeq
    ) {
        log.info("尝试查看{}的 seq为 {}到 {} 的消息",conversationId,minSeq,maxSeq);
        log.error("{}",conversationId);
        String key = "timeline:" + conversationId;

        List<Message> result = new ArrayList<>();

        // 一次性从 Redis 拉对应 seq 范围内的所有：seq -> messageId
        // WITHSCORES 形式：value=messageId, score=seq
        Set<ZSetOperations.TypedTuple<String>> cachedTuples =
                redis.opsForZSet().rangeByScoreWithScores(key, minSeq + 1, maxSeq);

        // 建立一个 Map<seq, messageId> 方便查找
        Map<Long, Long> redisSeqToMsgId = new HashMap<>();
        if (cachedTuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : cachedTuples) {
                Long seq = (long) tuple.getScore().longValue();
                Long messageId = Long.valueOf(tuple.getValue());
                redisSeqToMsgId.put(seq, messageId);
            }
        }

        // 遍历 minSeq+1 到 maxSeq
        for (Long seq = minSeq + 1; seq <= maxSeq; seq++) {

            if (redisSeqToMsgId.containsKey(seq)) {
                // ----- Redis 有数据：取 messageId -----
                Long messageId = redisSeqToMsgId.get(seq);

                // 根据 messageId 获取 message
                Message msg = messageCacheService.getMessageById(messageId);
                if (msg != null) {
                    result.add(msg);
                }

            } else {
                // ----- Redis 没有：走数据库，根据 seq 和 conversationId -----
                log.info("Redis 没有：走数据库，根据 seq 和 conversationId");
                Message msg = messageRepository.selectBySeqCon(seq,conversationId);
                if (msg != null) {
                    result.add(msg);
                }
            }
        }

        return result;
    }






}