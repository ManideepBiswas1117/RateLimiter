//package com.ratelimiter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RedisConnectionChecker implements CommandLineRunner {
//
//    private final RedisConnectionFactory redisConnectionFactory;
//
//    @Value("${spring.redis.host}")
//    private String redisHost;
//
//    @Value("${spring.redis.port}")
//    private int redisPort;
//
//    public RedisConnectionChecker(RedisConnectionFactory redisConnectionFactory) {
//        this.redisConnectionFactory = redisConnectionFactory;
//    }
//
//    @Override
//    public void run(String... args) {
//        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
//            String ping = connection.ping();
//            
//            System.out.println("✅ Successfully connected to Redis at " + redisHost + ":" + redisPort + " | PING: " + ping);
//            System.out.println("🔍 Redis host from env: " + redisHost);
//
//        } catch (Exception e) {
//            System.err.println("❌ Failed to connect to Redis at " + redisHost + ":" + redisPort);
//            e.printStackTrace();
//        }
//    }
//}
