package com.chanper.gulimallcart.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserInfoVo {
    /**
     * 已登录用户在数据库中的ID
     */
    private Long userId;

    /**
     * 存储用户名
     */
    private String username;

    /**
     * 分配一个临时的user-key
     */
    private String userKey;

    /**
     * 判断是否是临时用户
     */
    private boolean tempUser = false;
}
