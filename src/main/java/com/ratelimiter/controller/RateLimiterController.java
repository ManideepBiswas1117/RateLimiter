package com.ratelimiter.controller;

import org.springframework.web.bind.annotation.*;

import com.ratelimiter.algo.SlidingWindow;
import com.ratelimiter.algo.TokenBucket;

@RestController
@RequestMapping("/api/rate-limiter")
public class RateLimiterController {

    private final SlidingWindow slidingLimiter = new SlidingWindow(2, 5);
    private final TokenBucket tokenLimiter = new TokenBucket(10, 2); 

    @GetMapping("/sliding")
    public String checkSlidingWindow() {
        return slidingLimiter.allow()
                ? "Sliding :  Request allowed"
                : "Sliding :  Rate limit exceeded";
    }

    @GetMapping("/token")
    public String checkTokenBucket() {
        return tokenLimiter.allow()
                ? "Token :  Request allowed"
                : "Token :  Rate limit exceeded";
    }
}
