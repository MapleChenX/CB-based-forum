package com.example.controller;

import com.example.common.Const;
import com.example.entity.RestBean;
import com.example.entity.dto.Topic;
import com.example.entity.vo.request.AllTopicSearchReq;
import com.example.entity.vo.request.AllUserSearchReq;
import com.example.entity.vo.request.AddUserReq;
import com.example.entity.vo.request.UpdateUserReq;
import com.example.entity.vo.response.AllPostsResp;
import com.example.entity.vo.response.AllUserResp;
import com.example.entity.vo.response.TopicDetailVO;
import com.example.service.AdminService;
import com.example.service.TopicService;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Resource
    AdminService service;

    @Resource
    TopicService topicService;

    @RequestMapping("/all-user")
    public RestBean<AllUserResp> showAllUser(@RequestParam Integer page,
                                             @RequestParam Integer size,
                                             @RequestBody AllUserSearchReq req){
        return RestBean.success(service.findAllUser(page, size, req));
    }

    @RequestMapping("/add-user")
    public RestBean<AllUserResp> addUser(@RequestBody @Valid AddUserReq req){
        service.addUser(req);
        return RestBean.success();
    }

    @RequestMapping("/update-user")
    public RestBean<AllUserResp> updateUser(@RequestBody UpdateUserReq req){
        service.updateUser(req);
        return RestBean.success();
    }

    @GetMapping("/delete-user")
    public RestBean<Void> deleteUser(@RequestParam Integer uid){
        service.deleteUser(uid);
        return RestBean.success();
    }

    @RequestMapping("/all-topic")
    public RestBean<AllPostsResp> showAllTopic(@RequestParam Integer page,
                                               @RequestParam Integer size,
                                               @RequestBody AllTopicSearchReq req){
        return RestBean.success(service.findAllTopic(page, size, req));
    }

    @GetMapping("/topic")
    public RestBean<Topic> topic(@RequestParam @Min(0) int tid){
        Topic topic = topicService.getTopic(tid);
        return RestBean.success(topic);
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
