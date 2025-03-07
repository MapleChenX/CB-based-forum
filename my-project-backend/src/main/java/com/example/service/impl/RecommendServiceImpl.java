package com.example.service.impl;

import com.example.entity.UserInteraction;
import com.example.service.RecommendService;
import com.example.utils.ContentBasedRecommendationModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Resource
    ContentBasedRecommendationModel contentBasedRecommendationModel;

    public List<Integer> similarRecommend(Integer uid){
        return contentBasedRecommendationModel.recommendPosts(uid);
    }

    @Override
    public List<Integer> recommendSimilarPosts(Integer uid, Integer topicId) {
        return contentBasedRecommendationModel.recommendSimilarPosts(uid, topicId);
    }

}
