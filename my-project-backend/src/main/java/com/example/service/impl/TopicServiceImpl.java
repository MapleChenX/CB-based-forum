package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.*;
import com.example.entity.vo.request.AddCommentVO;
import com.example.entity.vo.request.TopicCreateVO;
import com.example.entity.vo.request.TopicUpdateVO;
import com.example.entity.vo.response.CommentVO;
import com.example.entity.vo.response.TopicDetailVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import com.example.mapper.*;
import com.example.service.NotificationService;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    TopicTypeMapper mapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    TopicCommentMapper commentMapper;

    @Resource
    StringRedisTemplate template;

    @Resource
    NotificationService notificationService;

    private Set<Integer> types = null;

    // @PostConstruct 是一个Java注解，它表示该注解修饰的方法会在依赖注入完成后，且在类的构造函数执行后立即执行。这个注解通常用于进行一些初始化操作。
    @PostConstruct
    private void initTypes() {
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TopicType> listTypes() {
        // 因为参数为 null，所以它不会应用任何过滤条件，将返回所有的记录。
        return mapper.selectList(null);
    }

    @Override
    public String createTopic(int uid, TopicCreateVO vo) {
        if (!textLimitCheck(vo.getContent(), 20000))
            return "文章内容太多，发文失败！";
        if (!types.contains(vo.getType()))
            return "文章类型非法！";
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        // 十分钟能发100篇文章
        if (!flowUtils.limitPeriodCounterCheck(key, 100, 600))
            return "发文频繁，请稍后再试！";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo, topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if (this.save(topic)) {
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        } else {
            return "内部错误，请联系管理员！";
        }
    }

    @Override
    public String updateTopic(int uid, TopicUpdateVO vo) {
        if (!textLimitCheck(vo.getContent(), 20000))
            return "文章内容太多，发文失败！";
        if (!types.contains(vo.getType()))
            return "文章类型非法！";
        baseMapper.update(null, Wrappers.<Topic>update()
                .eq("uid", uid)
                .eq("id", vo.getId())
                .set("title", vo.getTitle())
                .set("content", vo.getContent().toString())
                .set("type", vo.getType())
        );
        return null;
    }

    @Override
    public String createComment(int uid, AddCommentVO vo) {
        if (!textLimitCheck(JSONObject.parseObject(vo.getContent()), 50000))
            return "评论内容太多，发表失败！";
        String key = Const.FORUM_TOPIC_COMMENT_COUNTER + uid;
        if (!flowUtils.limitPeriodCounterCheck(key, 20, 1))
            return "发表评论频繁，请稍后再试！";

        TopicComment comment = new TopicComment();
        comment.setUid(uid);
        BeanUtils.copyProperties(vo, comment);
        comment.setTime(new Date());
        commentMapper.insert(comment);

        // 拿到帖子信息
        Topic topic = baseMapper.selectById(vo.getTid());
        // 拿到自己的信息
        Account account = accountMapper.selectById(uid);
        // 有引用才需要通知人家（引用就是回复人家的评论）
        if (vo.getQuote() > 0) {
            // 拿到被引用的评论
            TopicComment com = commentMapper.selectById(vo.getQuote());
            // 被引用的评论不是自己的，就通知
            if (!Objects.equals(account.getId(), com.getUid())) {
                notificationService.addNotification( // 回复评论
                        com.getUid(),
                        "您有新的帖子评论回复",
                        account.getUsername() + " 回复了你发表的评论，快去看看吧！",
                        "success", "/index/topic-detail/" + com.getTid()
                );
            }
        } else if (!Objects.equals(account.getId(), topic.getUid())) { // 回复帖子
            notificationService.addNotification(
                    topic.getUid(),
                    "您有新的帖子回复",
                    account.getUsername() + " 回复了你发表主题: " + topic.getTitle() + "，快去看看吧！",
                    "success", "/index/topic-detail/" + topic.getId()
            );
        }
        return null;
    }

    @Override
    public List<CommentVO> comments(int tid, int pageNumber) {
        // 评论展示用分页查询
        Page<TopicComment> page = Page.of(pageNumber, 10);
        commentMapper.selectPage(page, Wrappers.<TopicComment>query().eq("tid", tid));

        return page.getRecords().stream().map(dto -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(dto, vo);

            if (dto.getQuote() > 0) { // 默认-1表示没有引用
                // 从数据库查询引用的那个评论
                TopicComment comment = commentMapper.selectOne(Wrappers.<TopicComment>query()
                        .eq("id", dto.getQuote()).orderByAsc("time"));

                // 处理数据库的信息，回显的vo中的quote字段是一个字符串，它的值是引用的评论的文字内容
                if (comment != null) {
                    JSONObject object = JSONObject.parseObject(comment.getContent());
                    StringBuilder builder = new StringBuilder();
                    this.shortContent(object.getJSONArray("ops"), builder, ignore -> {
                    });
                    vo.setQuote(builder.toString());
                } else {
                    vo.setQuote("此评论已被删除");
                }
            }

            // 评论人身份信息
            CommentVO.User user = new CommentVO.User();
            this.fillUserDetailsByPrivacy(user, dto.getUid());
            vo.setUser(user);
            log.warn(vo.toString());
            return vo;
        }).toList();
    }

    @Override
    public void deleteComment(int id, int uid) {
        commentMapper.delete(Wrappers.<TopicComment>query().eq("id", id).eq("uid", uid));
    }

    @Override
    public List<TopicPreviewVO> listTopicCollects(int uid) {
        return baseMapper.collectTopics(uid)
                .stream()
                .map(topic -> {
                    TopicPreviewVO vo = new TopicPreviewVO();
                    BeanUtils.copyProperties(topic, vo);
                    return vo;
                })
                .toList();
    }

    @Override
    public List<TopicPreviewVO> listTopicByPage(int pageNumber, int type) {
        // 根据页码和类型拼接缓存key
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" + type;
        // 从缓存中获取list
        List<TopicPreviewVO> list = cacheUtils.takeListFromCache(key, TopicPreviewVO.class);
        // 如果缓存中有，直接返回
        if (list != null)
            return list;
        // 创建分页对象
        Page<Topic> page = Page.of(pageNumber, 10);
        // 根据类型查询
        if (type == 0) {
            baseMapper.selectPage(page, Wrappers.<Topic>query().orderByDesc("time"));
        } else {
            baseMapper.selectPage(page, Wrappers.<Topic>query().eq("type", type).orderByDesc("time"));
        }
        // 获取分页数据
        List<Topic> topics = page.getRecords();
        log.info("从数据库中获取数据 {} 条, {}", topics.size(), topics);
        // 如果没有数据，返回null
        if (topics.isEmpty()) return null;
        // 数据处理部分，将分页数据转换为Preview对象
        list = topics.stream().map(this::resolveToPreview).toList();
        // 将Preview对象存入缓存
        cacheUtils.saveListToCache(key, list, 3);
        return list;
    }

    @Override
    public List<TopicTopVO> listTopTopics() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id", "title", "time")
                .eq("top", 1));
        return topics.stream().map(topic -> {
            TopicTopVO vo = new TopicTopVO();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
    }

    @Override
    public TopicDetailVO getTopic(int tid, int uid) {
        TopicDetailVO vo = new TopicDetailVO();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic, vo);
        TopicDetailVO.Interact interact = new TopicDetailVO.Interact(
                hasInteract(tid, uid, "like"),
                hasInteract(tid, uid, "collect")
        );
        vo.setInteract(interact);
        TopicDetailVO.User user = new TopicDetailVO.User();
        vo.setUser(this.fillUserDetailsByPrivacy(user, topic.getUid()));
        vo.setComments(commentMapper.selectCount(Wrappers.<TopicComment>query().eq("tid", tid)));
        return vo;
    }

    /**
     * 由于论坛交互数据(如点赞、收藏等)更新可能会非常频繁
     * 为了减少数据库的压力，我们将交互数据先存储到 Redis 中
     * 然后通过延时任务将数据批量存储到 MySQL 中
     */
    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()) {
            // Redis 的哈希结构只能存储字符串类型的值，不能直接存储布尔值，所以需要将state转换为字符串
            // type是键，后面两个以键值对的方式作为值
            // 把数据先扔进redis，然后通过延时任务再扔进mysql
            template.opsForHash().put(type, interact.toKey(), Boolean.toString(state));
            // 延时任务
            this.saveInteractSchedule(type);
        }
    }

    // 当前用户是否与帖子交互
    private boolean hasInteract(int tid, int uid, String type) {
        String key = tid + ":" + uid;
        // 先从缓存中查
        if (template.opsForHash().hasKey(type, key))
            return Boolean.parseBoolean(template.opsForHash().entries(type).get(key).toString());
        // 不行再从数据库中查
        return baseMapper.userInteractCount(tid, uid, type) > 0;
    }

    // 首先，它定义了一个名为state的HashMap，用于存储每种类型的交互是否已经有一个待执行的延迟任务
    private static final Map<String, Boolean> state = new HashMap<>();
    // 然后，它定义了一个名为service的ScheduledExecutorService，用于执行延迟任务
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    // 保存交互调度
    private void saveInteractSchedule(String type) {
        // 如果没有延迟任务
        if (!state.getOrDefault(type, false)) {
            // 就添加一个新的延迟任务
            state.put(type, true);
            log.info("添加保存交互调度 {}", new Date().toString());
            // 3秒后执行保存操作
            service.schedule(() -> {
                this.saveInteract(type);
                state.put(type, false);
                log.info("保存交互信息成功 {}", new Date().toString());
            }, 3, TimeUnit.SECONDS);
        }
    }

    // 保存交互信息
    private void saveInteract(String type) {
        synchronized (type.intern()) {
            // 存储器
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();

            // 遍历redis获取点赞等信息
            // type是交互类型，k是用户与帖子的交互id，v是交互状态（点赞还是取消点赞）
            template.opsForHash().entries(type).forEach((k, v) -> {
                if (Boolean.parseBoolean(v.toString()))
                    check.add(Interact.parseInteract(k.toString(), type));
                else
                    uncheck.add(Interact.parseInteract(k.toString(), type));
            });

            // 添加还是删除交互
            if (!check.isEmpty())
                baseMapper.addInteract(check, type);
            if (!uncheck.isEmpty())
                baseMapper.deleteInteract(uncheck, type);
            template.delete(type);
        }
    }

    private <T> T fillUserDetailsByPrivacy(T target, int uid) {
        AccountDetails details = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy accountPrivacy = accountPrivacyMapper.selectById(uid);
        String[] ignores = accountPrivacy.hiddenFields();
        BeanUtils.copyProperties(account, target, ignores);
        BeanUtils.copyProperties(details, target, ignores);
        return target;
    }

    private TopicPreviewVO resolveToPreview(Topic topic) {
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()), vo);
        BeanUtils.copyProperties(topic, vo);
        vo.setLike(baseMapper.interactCount(topic.getId(), "like"));
        vo.setCollect(baseMapper.interactCount(topic.getId(), "collect"));

        // 从数据库中解析出图片链接，然后放到这个List中
        List<String> images = new ArrayList<>();
        // 方便拼接字符串，作文章预览用
        StringBuilder previewText = new StringBuilder();

        // JSONObject.parseObject(topic.getContent()) 将 topic 对象的 content 属性（一个 JSON 格式的字符串）解析为一个 JSONObject 对象。
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        // 提取文章预览内容和图片链接
        this.shortContent(ops, previewText, obj -> images.add(obj.toString()));

        vo.setText(previewText.length() > 300 ? previewText.substring(0, 300) : previewText.toString());
        vo.setImages(images);
        return vo;
    }

    // 数据库数据
    // {"ops":[{"insert":{"image":"http://localhost:8080/images/cache/20231103/f1915a87311d40beb9926c16c41f8230"}},{"insert":"\n"},{"insert":{"image":"http://localhost:8080/images/cache/20231103/9912f6aed02c4ca6950c4d17f5ff7e83"}},{"insert":"\n\n"}]}
    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandler) {
        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            // 通过判断类型来得知是图片还是文字
            // 这行代码检查 "insert" 键对应的值是否是一个字符串。如果是，那么它将这个值赋给 text。
            if (insert instanceof String text) {
                if (previewText.length() >= 300) continue;
                previewText.append(text);
            } else if (insert instanceof Map<?, ?> map) {
                // map.get("image")不存在就返回空，存在就在imageHandler中处理
                Optional.ofNullable(map.get("image")).ifPresent(imageHandler);
            }
        }
    }

    private boolean textLimitCheck(JSONObject object, int max) {
        if (object == null) return false;
        long length = 0;
        for (Object op : object.getJSONArray("ops")) {
            length += JSONObject.from(op).getString("insert").length();
            if (length > max) return false;
        }
        return true;
    }
}
