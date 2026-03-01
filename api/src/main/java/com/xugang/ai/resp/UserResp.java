package com.xugang.ai.resp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResp {
    
    private Long id;
    private String username;
    private String email;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
}
