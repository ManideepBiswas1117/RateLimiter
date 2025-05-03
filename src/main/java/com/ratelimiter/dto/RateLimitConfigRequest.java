package com.ratelimiter.dto;

public class RateLimitConfigRequest {
    private String user;
    private long limit;
    private long window;
    
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public long getLimit() {
		return limit;
	}
	public void setLimit(long limit) {
		this.limit = limit;
	}
	public long getWindow() {
		return window;
	}
	public void setWindow(long window) {
		this.window = window;
	}
	@Override
	public String toString() {
		return "RateLimitConfigRequest [user=" + user + ", limit=" + limit + ", window=" + window + "]";
	}

    
    
}
