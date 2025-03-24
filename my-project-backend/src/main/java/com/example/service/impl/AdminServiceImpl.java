package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.common.Const;
import com.example.entity.dto.Account;
import com.example.entity.dto.Topic;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.TopicService;
import com.example.utils.RabbitMQUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    AccountService accountService;
    @Resource
    TopicService topicService;

    @Resource
    private RabbitMQUtil rabbitMQUtil;

    @Override
    public List<Account> findAllUser() {
        return accountService.list(Wrappers.<Account>lambdaQuery().ne(Account::getRole, "admin"));
    }

    @Override
    public void deleteUser(int uid) {
        // 获取用户
        Account user = accountService.getOne(Wrappers.<Account>query().eq("id", uid));
        // 检查用户角色是否为 "admin"
        if (user != null && !user.getRole().equals("admin")) {
            // 删除用户
            accountService.remove(Wrappers.<Account>query().eq("id", uid));
            // 删除用户下的所有帖子
            topicService.remove(Wrappers.<Topic>query().eq("uid", uid));
        }
    }
    @Override
    public List<Topic> findAllTopic() {
        return topicService.list();
    }

    @Override
    public void deleteTopic(int tid) {
        // 删除帖子
        topicService.update(
                Wrappers.<Topic>lambdaUpdate()
                        .set(Topic::getIsDel, 1)
                        .eq(Topic::getId, tid));

        rabbitMQUtil.sendMessage(Const.POSTS_DEL_2_ES_MQ, String.valueOf(tid));
    }

    @Override
    public void rankUp(int uid) {
        accountService.update(Wrappers.<Account>lambdaUpdate().set(Account::getRole, "admin").eq(Account::getId, uid));
    }

    @Override
    public void setTop(int tid) {
        UpdateWrapper<Topic> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("top", 1).eq("id", tid);
        topicService.update(updateWrapper);
    }

    @Override
    public void downTop(int tid) {
        UpdateWrapper<Topic> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("top", 0).eq("id", tid);
        topicService.update(updateWrapper);
    }
}
