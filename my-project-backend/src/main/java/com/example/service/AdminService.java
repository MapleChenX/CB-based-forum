package com.example.service;

import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.entity.vo.response.NotificationVO;

import java.util.List;

public interface AdminService {
    List<Account> findAllUser();
    void deleteUser(int uid);
    List<Topic> findAllTopic();
    void deleteTopic(int tid);
    void rankUp(int uid);
    void setTop(int tid);
    void downTop(int tid);
}
