package com.example.controller;

import com.example.service.RecommendService;
import com.example.utils.Const;
import com.example.utils.ContentBasedRecommendationModel;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Resource
    RecommendService recommendService;

    /**
     * 相似贴推荐
     * @param uid
     * @param topicId
     * @return 推荐的相似贴 ids
     */
    @GetMapping("/similar/{topicId}")
    public List<Integer> similarRecommend(@RequestAttribute(Const.ATTR_USER_ID) int uid, @PathVariable Integer topicId){
        return recommendService.similarRecommend(uid, topicId);
    }

}
