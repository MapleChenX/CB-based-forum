package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.service.RecommendService;
import com.example.utils.Const;
import com.example.utils.ContentBasedRecommendationModel;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Resource
    @Lazy
    RecommendService recommendService;

    @Resource
    ContentBasedRecommendationModel contentBasedRecommendationModel;

    /**
     * 主页推荐
     * @param uid
     * @return 推荐的相似贴 ids
     */
    @GetMapping("/homeRecommendation")
    public List<Integer> homeRecommendation(@RequestAttribute(Const.ATTR_USER_ID) int uid){
        return recommendService.similarRecommend(uid);
    }

    /**
     * 相似贴推荐
     * @param uid
     * @param topicId
     * @return 推荐的相似贴 ids
     */
    @GetMapping("/similar/{topicId}")
    public RestBean<List<TopicPreviewVO>> similarRecommend(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                                           @PathVariable Integer topicId){
        List<TopicPreviewVO> topicPreviewVOS = recommendService.recommendSimilarPosts(uid, topicId);
        return topicPreviewVOS == null ? RestBean.failure(400, "无相似帖子推荐") : RestBean.success(topicPreviewVOS);
    }

    /**
     * 推荐系统全量初始化
     */
    @GetMapping("/recommendationInit")
    @PreAuthorize("hasRole('admin')")
    public RestBean<?> recommendationInit(){
        contentBasedRecommendationModel.updateTFIDF();
        return RestBean.success();
    }

}
