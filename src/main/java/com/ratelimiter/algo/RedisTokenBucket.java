package com.ratelimiter.algo;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class RedisTokenBucket {

    private final StringRedisTemplate redisTemplate;
    private final int capacity = 5;
    private final double refillRatePerSecond = .2;

    public RedisTokenBucket(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowRequest(String key) {
        String tokensKey = key + ":tokens";
        String lastRefillKey = key + ":lastRefill";
        long now = Instant.now().toEpochMilli();

        String tokensStr = redisTemplate.opsForValue().get(tokensKey);
        String lastRefillStr = redisTemplate.opsForValue().get(lastRefillKey);

        double tokens = tokensStr != null ? Double.parseDouble(tokensStr) : capacity;
        long lastRefillTime = lastRefillStr != null ? Long.parseLong(lastRefillStr) : now;

        double elapsedSeconds = (now - lastRefillTime) / 1000.0;
        tokens = Math.min(capacity, tokens + elapsedSeconds * refillRatePerSecond);

        if (tokens >= 1) {
            tokens -= 1;
            redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), 60, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(now), 60, TimeUnit.SECONDS);
            return true;
        } else {
            redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), 60, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(now), 60, TimeUnit.SECONDS);
            return false;
        }
    }
}
