package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.Const;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.dto.Topic;
import com.example.entity.vo.request.AddUserReq;
import com.example.entity.vo.request.AllTopicSearchReq;
import com.example.entity.vo.request.AllUserSearchReq;
import com.example.entity.vo.request.UpdateUserReq;
import com.example.entity.vo.response.AccountResp;
import com.example.entity.vo.response.AllPostsResp;
import com.example.entity.vo.response.AllUserResp;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import com.example.service.AdminService;
import com.example.service.TopicService;
import com.example.utils.RabbitMQUtil;
import com.example.utils.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    public AllUserResp findAllUser(Integer page, Integer size, AllUserSearchReq req) {
        Page<Account> pageRequest = new Page<>(page, size);

        LambdaQueryWrapper<Account> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(Account::getRole, "admin");

        if (req.getId() != null) {
            queryWrapper.eq(Account::getId, req.getId());
        }
        if (req.getUsername() != null && !req.getUsername().isEmpty()) {
            queryWrapper.like(Account::getUsername, req.getUsername());
        }
        if (req.getEmail() != null && !req.getEmail().isEmpty()) {
            queryWrapper.eq(Account::getEmail, req.getEmail());
        }
        if (req.getTimeStart() != null) {
            queryWrapper.ge(Account::getRegisterTime, req.getTimeStart());
        }
        if (req.getTimeEnd() != null) {
            queryWrapper.le(Account::getRegisterTime, req.getTimeEnd());
        }

        Page<Account> admin = accountService.page(pageRequest, queryWrapper);

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
    @Transactional
    public void addUser(AddUserReq req) {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator();
        int id = (int) snowflakeIdGenerator.nextId();
        Account account = new Account();
        account.setId(id);
        account.setUsername(req.getUsername());
        account.setPassword(req.getPassword());
        account.setEmail(req.getEmail());
        account.setRegisterTime(new Date()); // 注册时间
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setId(id);
        accountService.save(account);
        accountDetailsService.save(accountDetails);
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserReq req) {
        Account account = new Account();
        AccountDetails accountDetails = new AccountDetails();
        BeanUtils.copyProperties(req, account);
        BeanUtils.copyProperties(req, accountDetails);
        accountService.updateById(account);
        accountDetailsService.updateById(accountDetails);
    }

    @Override
    public void deleteUser(int uid) {
        Account user = accountService.getOne(Wrappers.<Account>query().eq("id", uid));
        if (user != null && !user.getRole().equals("admin")) {
            accountService.update(Wrappers.<Account>lambdaUpdate()
                    .set(Account::getIsDel, 1)
                    .eq(Account::getId, uid));
        }
    }

    @Override
    public AllPostsResp findAllTopic(Integer page, Integer size, AllTopicSearchReq req) {
        Page<Topic> pageRequest = new Page<>(page, size);

        LambdaQueryWrapper<Topic> queryWrapper = Wrappers.lambdaQuery();

        // 动态条件查询
        if (req.getId() != null) {
            queryWrapper.eq(Topic::getId, req.getId());
        }
        if (req.getType() != null) {
            queryWrapper.eq(Topic::getType, req.getType());
        }
        if (req.getTitle() != null && !req.getTitle().isEmpty()) {
            queryWrapper.like(Topic::getTitle, req.getTitle());
        }
        if (req.getUid() != null) {
            queryWrapper.eq(Topic::getUid, req.getUid());
        }
        if (req.getTimeStart() != null) {
            queryWrapper.ge(Topic::getTime, req.getTimeStart());
        }
        if (req.getTimeEnd() != null) {
            queryWrapper.le(Topic::getTime, req.getTimeEnd());
        }

        Page<Topic> pageData = topicService.page(pageRequest, queryWrapper);
        List<TopicPreviewVO> list = pageData.getRecords().stream()
                .map(e -> topicService.resolveToPreview(e))
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
