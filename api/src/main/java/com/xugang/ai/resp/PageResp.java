package com.xugang.ai.resp;

import lombok.Data;

import java.util.List;

@Data
public class PageResp<T> {
    private Long count;
    private List<T> list;
}
