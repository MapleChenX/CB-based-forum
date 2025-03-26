package com.example.entity.vo.response;

import lombok.Data;

import java.util.List;

@Data
public class AllPostsResp {
    List<TopicPreviewVO> posts;
    Long total;
    Long curPage;
    Long size;
    Long pages;
}
