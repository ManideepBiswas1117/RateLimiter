package com.ratelimiter.algo;

public class TokenBucket {
	private final int capacity;
	private final double refillRate;
	private double tokens;
	private long lastRefillTime;
	
	public TokenBucket(int capacity, double refillRate) {
		this.capacity=capacity;
		this.refillRate =refillRate;
		this.tokens=capacity;
		this.lastRefillTime=System.nanoTime();
	}
	
	public synchronized boolean allow() {
		long now = System.nanoTime();
		double secondsPassed = (now- lastRefillTime)*1000000000;
		tokens = Math.min(capacity, tokens + secondsPassed* refillRate);
		lastRefillTime = now;
		
		if(tokens>=1) {
			tokens-=1;
			return true;
		}
		return false;
	}

}
