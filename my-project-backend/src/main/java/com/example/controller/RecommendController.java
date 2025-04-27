package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.service.ESService;
import com.example.service.RecommendService;
import com.example.common.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@Tag(name="帖子推荐端")
public class RecommendController {

    @Resource
    @Lazy
    private RecommendService recommendService;

    /**
     * 主页推荐
     * @param uid
     * @return 推荐的相似贴 ids
     */
    @GetMapping("/homeRecommendation")
    @Operation(summary = "主页推荐")
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
    @Operation(summary = "相似贴推荐v1接口")
    public RestBean<List<TopicPreviewVO>> similarRecommend(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                                           @PathVariable Integer topicId){
        List<TopicPreviewVO> topicPreviewVOS = recommendService.recommendSimilarPosts(uid, topicId);
        return topicPreviewVOS == null ? RestBean.failure(400, "无相似帖子推荐") : RestBean.success(topicPreviewVOS);
    }

    /**
     * 相似贴推荐
     * @param uid
     * @param topicId
     * @return 推荐的相似贴 ids
     */
    @GetMapping("/similar/v2/{topicId}")
    @Operation(summary = "相似贴推荐v2接口")
    public RestBean<List<TopicPreviewVO>> similarRecommendV2(@RequestAttribute(Const.ATTR_USER_ID) int uid,
                                                           @PathVariable Integer topicId){
        List<TopicPreviewVO> topicPreviewVOS = recommendService.recommendSimilarPostsV2(uid, topicId);
        return topicPreviewVOS == null ? RestBean.failure(400, "无相似帖子推荐") : RestBean.success(topicPreviewVOS);
    }

}
