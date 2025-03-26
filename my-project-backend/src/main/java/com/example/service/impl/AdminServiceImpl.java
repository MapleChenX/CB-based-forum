package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Const;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.Topic;
import com.example.entity.vo.response.AccountResp;
import com.example.entity.vo.response.AllPostsResp;
import com.example.entity.vo.response.AllUserResp;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.TopicService;
import com.example.utils.RabbitMQUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    AccountService accountService;

    @Resource
    TopicService topicService;

    @Resource
    private AccountDetailsService accountDetailsService;

    @Resource
    private RabbitMQUtil rabbitMQUtil;

    @Override
    public AllUserResp findAllUser(Integer page, Integer size) {
        Page<Account> pageRequest = new Page<>(page, size);
        Page<Account> admin = accountService.page(pageRequest, Wrappers.<Account>lambdaQuery().ne(Account::getRole, "admin"));

        List<AccountResp> list = admin.getRecords().stream()
                .map(e -> {
                    AccountResp accountResp = new AccountResp();
                    AccountDetails details = accountDetailsService.findAccountDetailsById(e.getId());
                    BeanUtils.copyProperties(e, accountResp);
                    BeanUtils.copyProperties(details, accountResp);
                    return accountResp;
                })
                .toList();

        AllUserResp allUserResp = new AllUserResp();
        allUserResp.setUsers(list);
        allUserResp.setTotal(admin.getTotal());
        allUserResp.setCurPage(admin.getCurrent());
        allUserResp.setSize(admin.getSize());
        allUserResp.setPages(admin.getPages());
        return allUserResp;
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
    public AllPostsResp findAllTopic(Integer page, Integer size) {
        Page<Topic> pageRequest = new Page<>(page, size);
        Page<Topic> pageData = topicService.page(pageRequest, Wrappers.<Topic>lambdaQuery().orderByDesc(Topic::getTime));
        List<TopicPreviewVO> list = pageData.getRecords().stream()
                .map(e -> {
                    return topicService.resolveToPreview(e);
                })
                .toList();
        AllPostsResp allPostsResp = new AllPostsResp();
        allPostsResp.setPosts(list);
        allPostsResp.setTotal(pageData.getTotal());
        allPostsResp.setCurPage(pageData.getCurrent());
        allPostsResp.setSize(pageData.getSize());
        allPostsResp.setPages(pageData.getPages());
        return allPostsResp;
    }

    @Override
    public void deleteTopic(int tid) {
        // 删除帖子
        topicService.update(Wrappers.<Topic>lambdaUpdate()
                        .set(Topic::getIsDel, 1)
                        .eq(Topic::getId, tid));

        rabbitMQUtil.sendMessage(Const.POSTS_DEL_2_ES_MQ, String.valueOf(tid));
    }

    @Override
    public void rankUp(int uid) {
        accountService.update(Wrappers.<Account>lambdaUpdate()
                .set(Account::getRole, "admin")
                .eq(Account::getId, uid));
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
