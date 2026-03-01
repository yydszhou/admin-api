package com.xugang.ai.service;

import com.xugang.ai.common.util.Sha256Util;
import com.xugang.ai.entity.User;
import com.xugang.ai.security.TokenAuthService;
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
    private final TokenAuthService              tokenAuthService;

    @Value("${token.expire-hours:1}")
    private long tokenExpireHours;

    private static final String TOKEN_PREFIX = "token:userId:";

    /**
     * 生成 token（SHA256 of userId:username:timestamp）
     */
    public String generateToken(User user) {
        return Sha256Util.encrypt(user.getId() + ":" + user.getUsername() + ":" + System.currentTimeMillis());
    }

    /**
     * 保存 token 到 Redis，同时维护 token → userId 反查映射
     */
    public void saveToken(Long userId, String token) {
        long expireSeconds = tokenExpireHours * 3600;

        // 正向：userId → token
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);

        // 反向：token → userId（供 Filter 反查 username）
        tokenAuthService.saveTokenMapping(token, userId, expireSeconds);

        log.info("Token已保存到Redis: userId={}, expire={}hours", userId, tokenExpireHours);
    }

    /**
     * 根据用户ID获取 token
     */
    public String getToken(Long userId) {
        return redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
    }

    /**
     * 删除 token（退出登录）
     */
    public void removeToken(Long userId) {
        String token = getToken(userId);
        redisTemplate.delete(TOKEN_PREFIX + userId);
        if (token != null) {
            tokenAuthService.removeTokenMapping(token);
        }
        log.info("Token已从Redis删除: userId={}", userId);
    }

    /**
     * 检查 token 是否存在
     */
    public boolean hasToken(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + userId));
    }
}
