package com.xugang.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${token.expire-hours:1}")
    private long tokenExpireHours;
    
    private static final String TOKEN_PREFIX = "token:";
    
    /**
     * 保存token到Redis
     */
    public void saveToken(Long userId, String token) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, tokenExpireHours, TimeUnit.HOURS);
        log.info("Token已保存到Redis: userId={}, expire={}hours", userId, tokenExpireHours);
    }
    
    /**
     * 根据用户ID获取token
     */
    public String getToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 删除token
     */
    public void removeToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.info("Token已从Redis删除: userId={}", userId);
    }
    
    /**
     * 检查token是否存在
     */
    public boolean hasToken(Long userId) {
        String key = TOKEN_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

