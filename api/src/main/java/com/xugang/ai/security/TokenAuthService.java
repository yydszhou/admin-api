package com.xugang.ai.security;

import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Token 与 userId / username 的双向绑定服务
 *
 * Redis 结构：
 *   key  = "token:userId:{userId}"   → value = token（登录时写入）
 *   key  = "token:map:{token}"       → value = userId（方便反查）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserMapper userMapper;

    private static final String TOKEN_MAP_PREFIX = "token:map:";

    /**
     * 保存 token → userId 的反查映射（登录时由 TokenService 调用）
     */
    public void saveTokenMapping(String token, Long userId, long expireSeconds) {
        String key = TOKEN_MAP_PREFIX + token;
        redisTemplate.opsForValue().set(key, String.valueOf(userId),
                expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 根据 token 查找 username
     * 链路：token → Redis 查 userId → DB 查 username
     */
    public String getUsernameByToken(String token) {
        if (!StringUtils.hasText(token)) return null;

        String key = TOKEN_MAP_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (!StringUtils.hasText(userIdStr)) {
            return null;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userMapper.selectById(userId);
            if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
                return null;
            }
            return user.getUsername();
        } catch (NumberFormatException e) {
            log.error("Redis 中 token 对应的 userId 格式非法: {}", userIdStr);
            return null;
        }
    }

    /**
     * 删除 token 映射（退出登录时调用）
     */
    public void removeTokenMapping(String token) {
        redisTemplate.delete(TOKEN_MAP_PREFIX + token);
    }
}
