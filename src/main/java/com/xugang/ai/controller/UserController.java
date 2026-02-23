package com.xugang.ai.controller;

import com.xugang.ai.common.ApiResponse;
import com.xugang.ai.req.RegisterReq;
import com.xugang.ai.resp.UserResp;
import com.xugang.ai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ApiResponse<UserResp> register(@Valid @RequestBody RegisterReq req) {
        log.info("注册请求: username={}, traceId={}", req.getUsername(), req.getTraceId());
        
        UserResp resp = userService.register(req);
        
        return ApiResponse.success("注册成功", resp, req.getTraceId());
    }
}
