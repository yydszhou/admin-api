package com.xugang.ai.service;

import com.xugang.ai.common.enums.ResultCode;
import com.xugang.ai.common.exception.BizException;
import com.xugang.ai.entity.Role;
import com.xugang.ai.entity.User;
import com.xugang.ai.mapper.UserMapper;
import com.xugang.ai.req.LoginReq;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.resp.LoginResp;
import com.xugang.ai.resp.UserInfoResp;
import com.xugang.ai.resp.UserResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper      userMapper;
    private final TokenService    tokenService;
    private final RbacService     rbacService;
    private final PasswordEncoder passwordEncoder;

    // ----------------------------------------------------------------
    // 注册
    // ----------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public UserResp register(RegisterReq req) {
        log.info("用户注册: username={}", req.getUsername());

        if (userMapper.countByUsername(req.getUsername()) > 0) {
            log.warn("用户名已存在: username={}", req.getUsername());
            throw new BizException(ResultCode.USERNAME_EXISTS);
        }
        if (userMapper.countByEmail(req.getEmail()) > 0) {
            log.warn("邮箱已被注册: email={}", maskEmail(req.getEmail()));
            throw new BizException(ResultCode.EMAIL_EXISTS);
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        // 存储前使用 BCrypt 加密（前端明文传输）
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功: userId={}", user.getId());

        return convertToResp(user);
    }

        // ----------------------------------------------------------------
        // 登录（从数据库实时加载角色 + 权限）
        // 密码为明文，由 BCryptPasswordEncoder.matches() 与数据库 hash 比对
        // ----------------------------------------------------------------
    public LoginResp login(LoginReq req) {
        log.info("用户登录: username={}", req.getUsername());

        User user = userMapper.selectByUsername(req.getUsername());
        if (user == null) {
            log.warn("用户不存在: username={}", req.getUsername());
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("账号已禁用: username={}", req.getUsername());
            throw new BizException(ResultCode.USER_DISABLED);
        }

        // BCrypt 比对
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("密码错误: username={}", req.getUsername());
            throw new BizException(ResultCode.PASSWORD_ERROR);
        }

        // ---- 实时从数据库加载角色与权限 ----
        List<String> roleCodes  = rbacService.loadRolesByUserId(user.getId())
                .stream().map(Role::getRoleCode).toList();
        List<String> permCodes  = rbacService.loadPermissionCodesByUserId(user.getId());

        // 生成 token 并缓存到 Redis
        String token = tokenService.generateToken(user);
        tokenService.saveToken(user.getId(), token);

        log.info("用户登录成功: userId={}, roles={}", user.getId(), roleCodes);

        return convertToLoginResp(user, token, roleCodes, permCodes);
    }

    // ----------------------------------------------------------------
    // 查询当前用户信息（含权限）- 按 userId
    // ----------------------------------------------------------------
    public UserInfoResp getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return buildUserInfoResp(user);
    }

    // ----------------------------------------------------------------
    // 查询当前用户信息（含权限）- 按 username（供 Controller 使用）
    // ----------------------------------------------------------------
    public UserInfoResp getUserInfoByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return buildUserInfoResp(user);
    }

    // ----------------------------------------------------------------
    // 删除用户（逻辑删除）
    // ----------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        userMapper.deleteById(userId);
        log.info("用户已删除: userId={}", userId);
    }

    // ----------------------------------------------------------------
    // 内部工具方法
    // ----------------------------------------------------------------
    private UserInfoResp buildUserInfoResp(User user) {
        List<String> roleCodes = rbacService.loadRolesByUserId(user.getId())
                .stream().map(Role::getRoleCode).toList();
        List<String> permCodes = rbacService.loadPermissionCodesByUserId(user.getId());

        UserInfoResp resp = new UserInfoResp();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setStatus(user.getStatus());
        resp.setRoles(roleCodes);
        resp.setPermissions(permCodes);
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }

    private UserResp convertToResp(User user) {
        UserResp resp = new UserResp();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setCreate_time(user.getCreateTime());
        resp.setUpdate_time(user.getUpdateTime());
        return resp;
    }

    private LoginResp convertToLoginResp(User user, String token,
                                         List<String> roles, List<String> permissions) {
        LoginResp resp = new LoginResp();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setToken(token);
        resp.setRoles(roles);
        resp.setPermissions(permissions);
        resp.setCreateTime(user.getCreateTime());
        return resp;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = local.length() > 2 ? local.substring(0, 2) + "***" : "***";
        return masked + "@" + parts[1];
    }
}
