package com.xugang.ai.resp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RbacPermissionTreeResp {
    private String module;
    private List<RbacPermissionItemResp> children = new ArrayList<>();
}
