package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Interact;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.entity.vo.response.*;
import com.example.service.TopicService;
import com.example.service.WeatherService;
import com.example.common.Const;
import com.example.utils.ControllerUtils;
import com.example.utils.SensitiveWordFilter;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
@Tag(name="帖子相关")
public class ForumController {

    @Resource
    WeatherService service;

    @Resource
    TopicService topicService;

    @Resource
    ControllerUtils utils;

    @Resource
        private SensitiveWordFilter sensitiveWordFilter;

    @GetMapping("/weather")
    @Operation(summary = "天气展示")
    public RestBean<WeatherVO> weather(double longitude, double latitude){
        WeatherVO vo = service.fetchWeather(longitude, latitude);
        return vo == null ?
                RestBean.failure(400, "获取地理位置信息与天气失败，请联系管理员！") : RestBean.success(vo);
    }

    @GetMapping("/types")
    @Operation(summary = "帖子类型列表")
    public RestBean<List<TopicTypeVO>> listTypes(){
        return RestBean.success(topicService
                .listTypes()
                .stream()
                .map(type -> type.asViewObject(TopicTypeVO.class))
                .toList());
    }

    @PostMapping("/create-topic") //帖子数据是个JSON，图片url也在其中
    @Operation(summary = "新增帖子")
    public RestBean<Void> createTopic(@Valid @RequestBody TopicCreateVO vo,
                                      @RequestAttribute(Const.ATTR_USER_ID) int id) {
        return utils.messageHandle(() -> topicService.createTopic(id, vo));
    }

    @GetMapping("/list-topic")
    @Operation(summary = "帖子列表")
    public RestBean<List<TopicPreviewVO>> listTopic(@RequestParam @Min(0) int page,
                                                    @RequestParam @Min(0) int type) {
        List<TopicPreviewVO> topicPreviewVOS = topicService.listTopicByPage(page + 1, type);
        topicPreviewVOS.forEach(topic -> {
            topic.setTitle(SensitiveWordHelper.replace(topic.getTitle()));
            topic.setText(SensitiveWordHelper.replace(topic.getText()));
        });
        return RestBean.success(topicPreviewVOS);
    }

    @GetMapping("/top-topic")
    @Operation(summary = "置顶帖子列表展示")
    public RestBean<List<TopicTopVO>> topTopic(){
        return RestBean.success(topicService.listTopTopics());
    }

    /**
     * 获取帖子详情
     * @param tid 帖子 id
     * @param id 当前用户 id
     * @return 帖子详情
     */
    @GetMapping("/topic")
    @Operation(summary = "帖子详情信息")
    public RestBean<TopicDetailVO> topic(@RequestParam @Min(0) int tid,
                                         @RequestAttribute(Const.ATTR_USER_ID) int id){
        TopicDetailVO topic = topicService.getTopic(tid, id);
        topic.setContent(SensitiveWordHelper.replace(topic.getContent()));
        topic.setTitle(SensitiveWordHelper.replace(topic.getTitle()));
        return RestBean.success(topic);
    }

    /**
     * 交互
     * @param tid 帖子 id
     * @param type 是点赞还是收藏
     * @param state 是点赞还是取消点赞
     * @param id 当前用户 id
     * @return 交互结果
     */
    @GetMapping("/interact")
    @Operation(summary = "点赞和收藏")
    public RestBean<Void> interact(@RequestParam @Min(0) int tid,
                                   @RequestParam @Pattern(regexp = "(like|collect)") String type,
                                   @RequestParam boolean state,
                                   @RequestAttribute(Const.ATTR_USER_ID) int id) {
        topicService.interact(new Interact(tid, id, new Date(), type), state);
        return RestBean.success();
    }

    @GetMapping("/collects")
    @Operation(summary = "收藏列表展示")
    public RestBean<List<TopicPreviewVO>> collects(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(topicService.listTopicCollects(id));
    }

    @PostMapping("/update-topic")
    @Operation(summary = "编辑帖子")
    public RestBean<Void> updateTopic(@Valid @RequestBody TopicUpdateVO vo,
                                      @RequestAttribute(Const.ATTR_USER_ID) int id){
        return utils.messageHandle(() -> topicService.updateTopic(id, vo));
    }

    /**
     * 添加评论
     * @param vo 评论内容
     * @param id 当前用户 id
     * @return 评论结果
     */
    @PostMapping("/add-comment")
    @Operation(summary = "评论")
    public RestBean<Void> addComment(@Valid @RequestBody AddCommentVO vo,
                                     @RequestAttribute(Const.ATTR_USER_ID) int id){
        return utils.messageHandle(() -> topicService.createComment(id, vo));
    }

    /**
     * 获取评论
     * @param tid 帖子 id
     * @param page 页码
     */
    @GetMapping("/comments")
    @Operation(summary = "帖子评论展示")
    public RestBean<List<CommentVO>> comments(@RequestParam @Min(0) int tid,
                                              @RequestParam @Min(0) int page){
        List<CommentVO> comments = topicService.comments(tid, page + 1);
        comments.forEach(comment -> comment.setContent(SensitiveWordHelper.replace(comment.getContent())));
        return RestBean.success(comments);
    }

    /**
     * 删除自己的评论
     * @param id 评论 id
     * @param uid 当前用户 id
     */
    @GetMapping("/delete-comment")
    @Operation(summary = "删除评论")
    public RestBean<Void> deleteComment(@RequestParam @Min(0) int id,
                                        @RequestAttribute(Const.ATTR_USER_ID) int uid){
        topicService.deleteComment(id, uid);
        return RestBean.success();
    }

    /**
     * 帖子搜索
     * @param keyword 关键字
     * @param page 页码
     * @param size 偏移量
     */
    @GetMapping("/search/{keyword}")
    @Operation(summary = "帖子搜索")
    public RestBean<List<TopicPreviewVO>> search(@PathVariable String keyword,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        List<TopicPreviewVO> res = topicService.search(keyword, page, size).stream()
                .peek(e -> {
                    e.setTitle(SensitiveWordHelper.replace(e.getTitle()));
                    e.setText(SensitiveWordHelper.replace(e.getText()));
                })
                .toList();
        return RestBean.success(res);
    }
}
