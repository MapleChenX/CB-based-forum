package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.AccountPrivacy;
import com.example.entity.vo.request.PrivacySaveVO;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountPrivacyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacy> implements AccountPrivacyService {

    @Override
    @Transactional
    public void savePrivacy(int id, PrivacySaveVO vo) {
        // 根据id获取隐私信息
        AccountPrivacy privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
        // 获取状态
        boolean status = vo.isStatus();
        // 根据类型设置隐私信息
        switch (vo.getType()) {
            case "phone" -> privacy.setPhone(status);
            case "email" -> privacy.setEmail(status);
            case "gender" -> privacy.setGender(status);
            case "wx" -> privacy.setWx(status);
            case "qq" -> privacy.setQq(status);
        }
        // 保存或更新隐私信息
        this.saveOrUpdate(privacy);
    }

    public AccountPrivacy accountPrivacy(int id) {
        return Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
    }
}
