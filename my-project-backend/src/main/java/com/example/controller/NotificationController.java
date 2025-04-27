package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.response.NotificationVO;
import com.example.service.NotificationService;
import com.example.common.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@Tag(name="消息端")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    @Operation(summary = "消息列表展示")
    public RestBean<List<NotificationVO>> listNotification(@RequestAttribute(Const.ATTR_USER_ID) int id) {
        return RestBean.success(service.findUserNotification(id));
    }

    @GetMapping("/delete")
    @Operation(summary = "已读消息通知")
    public RestBean<List<NotificationVO>> deleteNotification(@RequestParam @Min(0) int id,
                                                             @RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserNotification(id, uid);
        return RestBean.success();
    }

    @GetMapping("/delete-all")
    @Operation(summary = "已读所有消息通知")
    public RestBean<List<NotificationVO>> deleteAllNotification(@RequestAttribute(Const.ATTR_USER_ID) int uid) {
        service.deleteUserAllNotification(uid);
        return RestBean.success();
    }
}
