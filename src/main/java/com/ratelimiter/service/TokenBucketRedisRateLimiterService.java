package com.ratelimiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBucketRedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final HashOperations<String, String, String> hashOps;

    @Autowired
    public TokenBucketRedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    public boolean allowRequest(String userId) {
        String configKey = "user:config:" + userId;
        String bucketKey = "token:bucket:" + userId;

        Map<String, String> config = hashOps.entries(configKey);
        if (config == null || config.isEmpty()) {
            return false;
        }

        long capacity = config.containsKey("limit") ? Long.parseLong(config.get("limit")) : 5;
        long refillInterval = config.containsKey("window") ? Long.parseLong(config.get("window")) : 60;

        double refillRatePerSecond = (double) capacity / refillInterval;
        long currentTime = Instant.now().getEpochSecond();

        String tokensStr = hashOps.get(bucketKey, "tokens");
        String lastRefillStr = hashOps.get(bucketKey, "lastRefill");

        double tokens = tokensStr == null ? capacity : Double.parseDouble(tokensStr);
        long lastRefill = lastRefillStr == null ? currentTime : Long.parseLong(lastRefillStr);

        // Calculate refill
        long secondsSinceLastRefill = currentTime - lastRefill;
        if (secondsSinceLastRefill > 0) {
            double tokensToAdd = secondsSinceLastRefill * refillRatePerSecond;
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefill = currentTime;
        }

        if (tokens >= 1) {
            tokens -= 1;

            // Update state in Redis
            hashOps.put(bucketKey, "tokens", String.valueOf(tokens));
            hashOps.put(bucketKey, "lastRefill", String.valueOf(lastRefill));
            hashOps.put(bucketKey, "lastRequestTime", String.valueOf(currentTime));

            // Optional: Set TTL for cleanup
            redisTemplate.expire(bucketKey, refillInterval * 2, TimeUnit.SECONDS);
            return true;
        } else {
            // Update only if refill happened
            if (secondsSinceLastRefill > 0) {
                hashOps.put(bucketKey, "tokens", String.valueOf(tokens));
                hashOps.put(bucketKey, "lastRefill", String.valueOf(lastRefill));
            }

            // Optional: Set TTL for cleanup
            redisTemplate.expire(bucketKey, refillInterval * 2, TimeUnit.SECONDS);
            return false;
        }
    }


    public Map<String, String> getUserConfig(String userId) {
        String configKey = "user:config:" + userId;
        String bucketKey = "token:bucket:" + userId;

        Map<String, String> response = new HashMap<>();
        Map<String, String> config = hashOps.entries(configKey);

        if (config == null || config.isEmpty()) {
            response.put("error", "No config found for user: " + userId);
            return response;
        }

        long capacity = config.containsKey("limit") ? Long.parseLong(config.get("limit")) : 5;
        long window = config.containsKey("window") ? Long.parseLong(config.get("window")) : 60;
        double refillRatePerSecond = (double) capacity / window;

        long currentTime = Instant.now().getEpochSecond();

        String tokensStr = hashOps.get(bucketKey, "tokens");
        String lastRefillStr = hashOps.get(bucketKey, "lastRefill");
        String lastRequestTimeStr = hashOps.get(bucketKey, "lastRequestTime");

        double tokens = (tokensStr == null) ? capacity : Double.parseDouble(tokensStr);
        long lastRefill = lastRefillStr == null ? currentTime : Long.parseLong(lastRefillStr);
        long lastRequestTime = lastRequestTimeStr == null ? currentTime : Long.parseLong(lastRequestTimeStr);

        long secondsSinceLastRefill = currentTime - lastRefill;

        
        double timeUntilNextRefill = Math.max(0, (1 - (secondsSinceLastRefill % refillRatePerSecond)));

        
        double refillTokens = secondsSinceLastRefill * refillRatePerSecond;
        tokens = Math.min(capacity, tokens + refillTokens);

        
        long timeSinceLastRequest = currentTime - lastRequestTime;

        response.put("limit", String.valueOf(capacity));
        response.put("window", String.valueOf(window));
        response.put("tokensRemaining", String.format("%.2f", tokens)); 
        response.put("lastRequestTime", String.valueOf(lastRequestTime));
        response.put("timeSinceLastRequest", String.valueOf(timeSinceLastRequest)); 
        response.put("nextRefillIn", String.format("%.2f", timeUntilNextRefill));

        
        hashOps.put(bucketKey, "tokens", String.valueOf(tokens));
        hashOps.put(bucketKey, "lastRefill", String.valueOf(currentTime));

        return response;
    }

    public void resetBucket(String userId) {
        redisTemplate.delete("token:bucket:" + userId); 
    }
}
