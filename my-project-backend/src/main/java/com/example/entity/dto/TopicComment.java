package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("db_topic_comment")
// 帖子下的所有评论
public class TopicComment {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer uid; // 谁在说话
    Integer tid; // 在哪个帖子下说话
    String content; // 说的什么
    Date time; // 什么时候说的
    Integer quote; // 对谁说
}
