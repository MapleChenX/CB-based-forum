package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Resource
    AdminService service;

    @GetMapping("/all-user")
    public RestBean<List<Account>> showAllUser(){
        List<Account> allAccount = service.findAllUser();
        return RestBean.success(allAccount);
    }

    @GetMapping("/all-topic")
    public RestBean<List<Topic>> showAllTopic(){
        List<Topic> allTopic = service.findAllTopic();
        return RestBean.success(allTopic);
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
