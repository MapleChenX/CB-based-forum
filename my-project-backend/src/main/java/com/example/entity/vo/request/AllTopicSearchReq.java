package com.example.entity.vo.request;

import lombok.Data;

import java.util.Date;

@Data
public class AllTopicSearchReq {
    Integer id;
    Integer type;
    String title;
    Date timeStart;
    Date timeEnd;
    Integer uid;
}
