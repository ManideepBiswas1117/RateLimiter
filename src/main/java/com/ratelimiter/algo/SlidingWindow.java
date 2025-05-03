package com.ratelimiter.algo;

import java.util.Deque;
import java.util.LinkedList;

public class SlidingWindow {
private final int maxReq;
private final long timeIntervalMillis;
private final Deque<Long> time;

public SlidingWindow(int maxReq, long timeIntervalSecs) {
	this.maxReq=maxReq;
	this.timeIntervalMillis = timeIntervalSecs*1000;
	this.time= new LinkedList<>();
}
public synchronized boolean allow() {
	long now = System.currentTimeMillis();
	
	while(!time.isEmpty()&& now-time.peekFirst()>timeIntervalMillis) {
		time.pollFirst();
	}
	if(time.size()<maxReq) {
		time.addLast(now);
		return true;
	}
	return false;
}

public static void main(String[] args) throws InterruptedException {
	SlidingWindow SlidingWindow = new SlidingWindow(2, 5l);
	Thread t1= new Thread(()->{
		while(true) {

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(SlidingWindow.allow() + " A : " +System.currentTimeMillis() / 1000);
			
		}
		
	});
	t1.start();
	while(true) {

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(SlidingWindow.allow() + " B : " +System.currentTimeMillis() / 1000);
	
	
	
}}
}
