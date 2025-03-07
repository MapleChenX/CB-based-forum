package com.example.service;


import java.util.List;

public interface RecommendService {
    List<Integer> similarRecommend(Integer uid);

    List<Integer> recommendSimilarPosts(Integer uid, Integer topicId);

}
