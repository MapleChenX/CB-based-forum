package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.Data;

@Data
@TableName("db_notification")
public class Notification implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id; // 通知id
    Integer uid; // 谁的通知
    String title; // 通知标题
    String content; // 通知内容
    String type; // 通知类型
    String url; // 通知链接，便于跳转到帖子
    String time; // 通知时间
}
