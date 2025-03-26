package com.example.entity.vo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddUserReq {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, message = "用户名最小长度不能小于6")
    String username;

    @NotBlank(message = "邮箱不能为空")
    @Size(min = 6, message = "邮箱最小长度不能小于6")
    String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码最小长度不能小于6")
    String password;
}
