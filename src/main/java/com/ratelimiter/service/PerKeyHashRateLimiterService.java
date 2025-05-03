package com.ratelimiter.service;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class PerKeyHashRateLimiterService {

    private final HashOperations<String, String, String> hashOps;

    public PerKeyHashRateLimiterService(StringRedisTemplate redisTemplate) {
        this.hashOps = redisTemplate.opsForHash();
    }

    public boolean isAllowed(String userId) {
        String key = "user:" + userId;
        Map<String, String> data = hashOps.entries(key);

        long currentTime = Instant.now().getEpochSecond();
        long lastTime = Long.parseLong(data.getOrDefault("lastRequestTime", "0"));
        int window = Integer.parseInt(data.getOrDefault("window", "60")); // default 60 seconds
        int limit = Integer.parseInt(data.getOrDefault("limit", "5"));   // default 5 requests
        int count = Integer.parseInt(data.getOrDefault("count", "0"));

        if (currentTime - lastTime > window) {
            // reset
            hashOps.put(key, "count", "1");
            hashOps.put(key, "lastRequestTime", String.valueOf(currentTime));
            return true;
        } else {
            if (count < limit) {
                hashOps.put(key, "count", String.valueOf(count + 1));
                return true;
            } else {
                return false;
            }
        }
    }
}
