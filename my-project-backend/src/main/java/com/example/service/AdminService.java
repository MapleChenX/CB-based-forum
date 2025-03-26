package com.example.service;

import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.entity.vo.response.AllPostsResp;
import com.example.entity.vo.response.AllUserResp;
import com.example.entity.vo.response.NotificationVO;

import java.util.List;

public interface AdminService {
    AllUserResp findAllUser(Integer page, Integer size);
    void deleteUser(int uid);
    AllPostsResp findAllTopic(Integer page, Integer size);
    void deleteTopic(int tid);
    void rankUp(int uid);
    void setTop(int tid);
    void downTop(int tid);
}
