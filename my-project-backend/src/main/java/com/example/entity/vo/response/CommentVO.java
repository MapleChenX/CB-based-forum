package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
// vo评论
public class CommentVO {
    int id;
    String content;
    Date time;
    String quote; // 引用的评论
    User user; //发言人信息

    @Data
    public static class User {
        Integer id;
        String username;
        String avatar;
        boolean gender;
        String qq;
        String wx;
        String phone;
        String email;
    }
}
