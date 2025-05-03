package com.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SlidingWindowRedisRateLimiterService {

    private final ZSetOperations<String, String> zSetOps;
    private final HashOperations<String, String, String> hashOps;

    @Autowired
    public SlidingWindowRedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.zSetOps = redisTemplate.opsForZSet();
        this.hashOps = redisTemplate.opsForHash();
    }

    public boolean isAllowed(String userId) {
        String configKey = "user:config:" + userId;
        String logKey = "sliding:log:" + userId;
        String lastRequestKey = "sliding:lastRequest:" + userId;

        String limitStr = hashOps.get(configKey, "limit");
        String windowStr = hashOps.get(configKey, "window");

        long limit = limitStr != null ? Long.parseLong(limitStr) : 5;
        long window = windowStr != null ? Long.parseLong(windowStr) : 60;

        long now = Instant.now().getEpochSecond();
        long windowStart = now - window;

//        zSetOps.removeRangeByScore(logKey, 0, windowStart);
        Set<ZSetOperations.TypedTuple<String>> entries = zSetOps.rangeWithScores(logKey, 0, -1);
        if (entries != null) {
            for (ZSetOperations.TypedTuple<String> entry : entries) {
                if (entry.getScore() != null && entry.getScore() <= windowStart) {
                    zSetOps.remove(logKey, entry.getValue());
                }
            }
        }

        Long currentCount = zSetOps.zCard(logKey);
        if (currentCount != null && currentCount < limit) {
            zSetOps.add(logKey, UUID.randomUUID().toString(), now);

            zSetOps.getOperations().expire(logKey, window + 10, TimeUnit.SECONDS);

            hashOps.put("sliding:lastRequest", userId, String.valueOf(now));
            return true;
        }

        return false;
    }

    public void updateUserConfig(String userId, long limit, long window) {
        String configKey = "user:config:" + userId;
        hashOps.put(configKey, "limit", String.valueOf(limit));
        hashOps.put(configKey, "window", String.valueOf(window));
    }

    public Map<String, String> getUserConfig(String userId) {
        String configKey = "user:config:" + userId;
        Map<String, String> config = hashOps.entries(configKey);

        long currentTime = Instant.now().getEpochSecond();

        String lastRequest = hashOps.get("sliding:lastRequest", userId);
        if (lastRequest != null) {
            long lastTime = Long.parseLong(lastRequest);
            long since = currentTime - lastTime;
            config.put("lastRequestTime", lastRequest);
            config.put("timeSinceLastRequest", String.valueOf(since));
        } else {
            config.put("lastRequestTime", "N/A");
            config.put("timeSinceLastRequest", "N/A");
        }

        String logKey = "sliding:log:" + userId;
        Long requestsInWindow = zSetOps.zCard(logKey);
        long windowStart = currentTime - (config.containsKey("window") ? Long.parseLong(config.get("window")) : 60);

        config.put("requestsInWindow", requestsInWindow != null ? String.valueOf(requestsInWindow) : "0");
        config.put("slidingWindowStartTime", String.valueOf(windowStart));

        return config;
    }
}
