package com.example.service;


import com.example.entity.vo.response.TopicPreviewVO;

import java.util.List;

public interface RecommendService {
    List<Integer> similarRecommend(Integer uid);

    List<Integer> recommendSimilarPostIds(Integer uid, Integer topicId);

    List<TopicPreviewVO> recommendSimilarPosts(Integer uid, Integer topicId);

}
