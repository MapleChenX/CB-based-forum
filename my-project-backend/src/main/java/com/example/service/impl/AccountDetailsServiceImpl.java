package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.entity.dto.AccountDetails;
import com.example.entity.vo.request.DetailsSaveVO;
import com.example.mapper.AccountDetailsMapper;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsService {

    @Resource
    AccountService service;

    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }

    @Override
    @Transactional // 这意味着 service.update() 和 this.saveOrUpdate() 这两个操作要么都成功，要么都不会对数据库产生影响。如果其中一个操作失败了，那么另一个操作对数据库的更改也会被回滚。
    public synchronized boolean saveAccountDetails(int id, DetailsSaveVO vo) {
        Account account = service.findAccountByNameOrEmail(vo.getUsername());
        if(account == null || account.getId() == id) {
            service.update()
                    .eq("id", id)
                    .set("username", vo.getUsername())
                    .update();
            this.saveOrUpdate(new AccountDetails(
                    id, vo.getGender(), vo.getPhone(),
                    vo.getQq(), vo.getWx(), vo.getDesc()
            ));
            return true;
        }
        return false;
    }
}
