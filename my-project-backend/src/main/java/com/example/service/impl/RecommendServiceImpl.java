package com.example.service.impl;

import com.example.entity.dto.Topic;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.mapper.TopicMapper;
import com.example.service.ESService;
import com.example.service.RecommendService;
import com.example.service.TopicService;
import com.example.utils.ContentBasedRecommendationModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RecommendServiceImpl implements RecommendService {

    @Resource
    private ContentBasedRecommendationModel contentBasedRecommendationModel;

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private TopicService topicService;

    @Resource
    private ESService esService;

    public List<Integer> similarRecommend(Integer uid){
        return contentBasedRecommendationModel.recommendPosts(uid);
    }

    @Override
    public List<Integer> recommendSimilarPostIds(Integer uid, Integer topicId) {
        return contentBasedRecommendationModel.recommendSimilarPosts(uid, topicId);
    }

    @Override
    public List<TopicPreviewVO> recommendSimilarPosts(Integer uid, Integer topicId) {
        List<Integer> RecommendedPostIds = recommendSimilarPostIds(uid, topicId);
        if (RecommendedPostIds.isEmpty()){
            return null;
        }
        ArrayList<TopicPreviewVO> res = new ArrayList<>();
        for (Integer postId : RecommendedPostIds) {
            Topic topic = topicMapper.selectById(postId);
            TopicPreviewVO topicPreviewVO = topicService.resolveToPreview(topic);
            res.add(topicPreviewVO);
        }
        return res;
    }

    @Override
    public List<TopicPreviewVO> recommendSimilarPostsV2(Integer uid, Integer topicId) {
        List<String> RecommendedPostIds = esService.getSimilarPostsById(String.valueOf(topicId));
        if (RecommendedPostIds == null || RecommendedPostIds.isEmpty()){
            log.error("！ES查询相似贴失败");
            return null;
        }

        ArrayList<TopicPreviewVO> res = new ArrayList<>();
        for (String postId : RecommendedPostIds) {
            Topic topic = topicMapper.selectById(postId);
            if (topic == null) continue;
            TopicPreviewVO topicPreviewVO = topicService.resolveToPreview(topic);
            res.add(topicPreviewVO);
        }
        return res;
    }

}
