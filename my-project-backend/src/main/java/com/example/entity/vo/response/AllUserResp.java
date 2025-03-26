package com.example.entity.vo.response;

import com.example.entity.dto.Account;
import lombok.Data;

import java.util.List;

@Data
public class AllUserResp {
    List<AccountResp> users;
    Long total;
    Long curPage;
    Long size;
    Long pages;
}
