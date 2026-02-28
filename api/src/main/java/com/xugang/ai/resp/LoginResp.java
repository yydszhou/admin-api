package com.xugang.ai.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResp {
    
    private Long user_id;
    private String username;
    private String email;
    private String token;
    private LocalDateTime create_time;
}
