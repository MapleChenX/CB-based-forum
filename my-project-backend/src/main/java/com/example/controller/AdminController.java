package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.entity.vo.response.AllPostsResp;
import com.example.entity.vo.response.AllUserResp;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Resource
    AdminService service;

    @GetMapping("/all-user")
    public RestBean<AllUserResp> showAllUser(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size){
        return RestBean.success(service.findAllUser(page, size));
    }

    @GetMapping("/all-topic")
    public RestBean<AllPostsResp> showAllTopic(@RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer size){
        return RestBean.success(service.findAllTopic(page, size));
    }

    @GetMapping("/delete-user")
    public RestBean<Void> deleteUser(@RequestParam int uid){
        service.deleteUser(uid);
        return RestBean.success();
    }

    @GetMapping("/delete-topic")
    public RestBean<Void> deleteTopic(@RequestParam int tid){
        service.deleteTopic(tid);
        return RestBean.success();
    }

    @GetMapping("/rank-up")
    public RestBean<Void> rankUp(@RequestParam int uid){
        service.rankUp(uid);
        return RestBean.success();
    }

    @GetMapping("/set-top")
    public RestBean<Void> setTop(@RequestParam int tid){
        service.setTop(tid);
        return RestBean.success();
    }

    @GetMapping("/down-top")
    public RestBean<Void> downTop(@RequestParam int tid){
        service.downTop(tid);
        return RestBean.success();
    }

}
