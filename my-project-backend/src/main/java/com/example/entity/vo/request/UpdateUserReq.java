package com.example.entity.vo.request;

import lombok.Data;


@Data
public class UpdateUserReq {
    Integer id;
    String username;
    String email;
    Integer gender;
    String phone;
    String qq;
    String wx;
    String desc;
}
