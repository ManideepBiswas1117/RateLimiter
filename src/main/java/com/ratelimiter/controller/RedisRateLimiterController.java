package com.ratelimiter.controller;

import com.ratelimiter.algo.RedisTokenBucket;
import com.ratelimiter.dto.RateLimitConfigRequest;
import com.ratelimiter.service.PerKeyHashRateLimiterService;
import com.ratelimiter.service.SlidingWindowRedisRateLimiterService;
import com.ratelimiter.service.TokenBucketRedisRateLimiterService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-limiter")
public class RedisRateLimiterController {

	@Autowired
	private PerKeyHashRateLimiterService perKeyHashRateLimiterService;
	@Autowired
	private SlidingWindowRedisRateLimiterService slidingWindowRedisRateLimiterService;
	@Autowired
	private TokenBucketRedisRateLimiterService tokenBucketRedisRateLimiterService;

    private final RedisTokenBucket redisLimiter;
    private final StringRedisTemplate redisTemplate; 

    public RedisRateLimiterController(RedisTokenBucket redisLimiter, StringRedisTemplate redisTemplate) {
        this.redisLimiter = redisLimiter;
        this.redisTemplate=redisTemplate;
    }

    
    
    @GetMapping("/per-user")
    public ResponseEntity<String> checkPerUserLimit(@RequestParam String user) {
        boolean allowed = perKeyHashRateLimiterService.isAllowed(user);
        if (allowed) {
            return ResponseEntity.ok("Allowed for user: " + user);
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded for user: " + user);
        }
    }
    
	@GetMapping("/redis-sliding")
	public ResponseEntity<String> checkRedisSliding(@RequestParam String user) {
	    boolean allowed = slidingWindowRedisRateLimiterService.isAllowed(user);
	    if (allowed) {
	        return ResponseEntity.ok("Allowed (Redis Sliding Window) for user: " + user);
	    } else {
	        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
	                .body("Rate limit exceeded (Redis Sliding Window) for user: " + user);
	    }
	}
	@PostMapping("/config")
	public ResponseEntity<String> updateUserRateLimit(@RequestBody RateLimitConfigRequest request) {
	    slidingWindowRedisRateLimiterService.updateUserConfig(
	        request.getUser(), request.getLimit(), request.getWindow()
	    );
	    return ResponseEntity.ok("Config updated for user: " + request.getUser());
	}
	@GetMapping("/config")
	public ResponseEntity<Map<String, String>> getUserRateLimit(@RequestParam String user,
	                                                            @RequestParam(defaultValue = "token") String algo) {
	    Map<String, String> config;

	    switch (algo.toLowerCase()) {
	        case "sliding":
	            config = slidingWindowRedisRateLimiterService.getUserConfig(user);
	            break;
	        case "token":
	        default:
	            config = tokenBucketRedisRateLimiterService.getUserConfig(user);
	            break;
	    }

	    if (config == null || config.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("error", "No config found for user: " + user));
	    }

	    return ResponseEntity.ok(config);
	}

	@GetMapping("/redis-token")
	public ResponseEntity<String> redisTokenBucketRateLimit(@RequestParam String user) {
	    boolean allowed = tokenBucketRedisRateLimiterService.allowRequest(user);
	    return allowed ? ResponseEntity.ok(" Request allowed")
	                   : ResponseEntity.status(429).body(" Rate limit exceeded");
	}



	
	

}
