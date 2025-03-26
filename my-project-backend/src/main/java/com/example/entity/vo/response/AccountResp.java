package com.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class AccountResp {
    Integer id;
    String username;
    String password;
    String email;
    String role;
    String avatar;
    Date registerTime;
    Integer gender;
    String phone;
    String qq;
    String wx;
    String desc;
}
