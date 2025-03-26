package com.example.entity.vo.request;

import lombok.Data;

import java.util.Date;

@Data
public class AllUserSearchReq {
    Integer id;
    String username;
    String email;
    Date timeStart;
    Date timeEnd;
}
