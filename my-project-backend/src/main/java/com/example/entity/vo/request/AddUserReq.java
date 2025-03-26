package com.example.entity.vo.request;

import lombok.Data;

@Data
public class AddUserReq {
    String username;
    String email;
    String password;
}
