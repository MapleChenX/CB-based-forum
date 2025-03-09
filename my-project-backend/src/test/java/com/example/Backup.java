package com.example;

import com.alibaba.fastjson2.JSON;
import com.example.common.MqConst;
import com.example.entity.UserInteraction;
import com.example.service.InteractService;
import com.example.service.TopicService;
import com.example.tfidf.TFIDF;
import com.example.utils.Const;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Backup {

    public static final Map<Integer, String> postContents = new HashMap<>();

    public static Map<Integer, Map<String, Double>> tfidfVectors = new HashMap<>();

    private static final int TOP_N = 10; // 返回的推荐数量
    private static final int BASE_N = 10; // 基准帖子数量

    private static final long TIME_DECAY_FACTOR = 1000 * 60 * 60 * 24 * 30L; // 30 days

    private static final Map<String, Double> INTERACTION_TYPE_WEIGHTS = Map.of(
            "view", 0.6,
            "comment", 0.8,
            "like", 1.0,
            "collect", 1.2
    );

    @Resource
    private InteractService interactService;

    @Resource
    private TopicService topicService;

    @Resource
    private StringRedisTemplate template;

    @Resource
    private TFIDF tfidf;


    @PostConstruct
    public void init() {
        boolean hasPostContent = Boolean.TRUE.equals(template.hasKey(Const.POST_CONTENT_BUCKET))
                && template.opsForHash().size(Const.POST_CONTENT_BUCKET) > 0;
        boolean hasTFIDF = Boolean.TRUE.equals(template.hasKey(Const.TFIDF_BUCKET))
                && template.opsForHash().size(Const.TFIDF_BUCKET) > 0;

        // 如果 Redis 里已经有数据，则不需要计算与同步
        if (hasPostContent && hasTFIDF) {
            log.info("redis got tf-idf vectors, no need to reload!");
            return;
        }
        log.info("redis no tf-idf vectors, data reloading...");
        updateTFIDF();
    }

    public void updateTFIDF() {
        long start = System.currentTimeMillis();
        System.out.println("Updating tfidfVectors...");
        postContents.clear();
        topicService.list().forEach(topic -> postContents.put(topic.getId(), optimizePostContent(topic.getContent())));
        tfidfVectors = calculateTFIDF(Backup.postContents);
        // 同步到redis
        tfidf.sync2Redis(postContents, tfidfVectors);
        System.out.println("tfidfVectors updated successfully!");
        System.out.println("time elapsed: " + (System.currentTimeMillis() - start) / 60000 + " minutes");
    }

//    @RabbitListener(queues = MqConst.POST_NEW)
    public void handlePostUpdate(String message) {
        System.out.println("Received post update notification: " + message);
        // Add logic to handle the post update notification
    }

    /**
     * 首页推荐
     * @param userId 用户ID
     *  interactions 用户交互数据列表
     *  postContents 帖子内容映射
     * @return 推荐的帖子ID列表
     */
    public List<Integer> recommendPosts(int userId) {
        List<UserInteraction> interactions = getUserInteractions(userId);

        // 当前用户的交互过的帖子ID集合
        Set<Integer> userPosts = interactions.stream()
                .map(UserInteraction::getPostId)
                .collect(Collectors.toSet());

        Map<Integer, Double> postWeights = new HashMap<>(); // 当前用户对帖子的喜好权重，key：postId，value：weight

        // 计算用户对帖子的喜好权重
        for (UserInteraction interaction : interactions) {
            postWeights.put(interaction.getPostId(),
                    postWeights.getOrDefault(interaction.getPostId(), 0.0) + calculateInteractionWeight(interaction));
        }

        // 基准帖子集合
        Set<Integer> basePosts = postWeights.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(BASE_N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<Integer, Double> postScores = new HashMap<>(); // 推荐帖子候选列表
        for (Integer postId : postContents.keySet()) {
            if (!userPosts.contains(postId)) { // 对用户没有交互过的帖子进行相似性分析
                // 当前帖子内容，用户交互过的帖子
                double score = calculateContentSimilarity(postId, basePosts);
                postScores.put(postId, score);
            }
        }

        // 推荐帖子集合
        return postScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(TOP_N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @NotNull
    private List<UserInteraction> getUserInteractions(int userId) {
        return interactService.getUserInteractions(userId).stream()
                .map(e ->{
                    UserInteraction userInteraction = new UserInteraction();
                    userInteraction.setUserId(e.getUid());
                    userInteraction.setPostId(e.getTid());
                    userInteraction.setInteractionType(e.getType());
                    userInteraction.setInteractionTime(e.getTime());
                    return userInteraction;
                })
                .toList();
    }

    /**
     * 根据当前浏览的帖子进行相似帖推荐
     * @param userId 当前用户ID
     * @param topicId 当前浏览的帖子ID
     * @return 推荐的帖子ID列表
     */
    public List<Integer> recommendSimilarPosts(int userId, Integer topicId) {
        List<UserInteraction> interactions = getUserInteractions(userId);
        if (interactions.isEmpty()) { // 用户没有交互过
            interactions = Collections.emptyList();
        }

        Set<Integer> userPosts = interactions.stream()
                .map(UserInteraction::getPostId)
                .collect(Collectors.toSet());

        Map<Integer, Double> postScores = new HashMap<>(); // 推荐帖子候选列表
        for (Integer postId : postContents.keySet()) {
            if (!userPosts.contains(postId)) { // 只处理用户未交互的帖子
                double score = calculateContentSimilarity(postId, Set.of(topicId));
                postScores.put(postId, score);
            }
        }

        // 推荐帖子集合
        return postScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(TOP_N)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String optimizePostContent(String jsonInput) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(jsonInput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }

        JsonNode opsNode = rootNode.get("ops");

        List<String> meaningfulData = new ArrayList<>();

        for (JsonNode node : opsNode) {
            if (node.has("insert") && node.get("insert").isTextual()) {
                meaningfulData.add(node.get("insert").asText());
            }
        }

        return String.join("。", meaningfulData);
    }


    /**
     * 对帖子的内容进行分词
     * @param content 要分词的内容
     * @return 分词后的集合
     */
    private Set<String> tokenize(String content) {
        return new JiebaSegmenter().process(content, JiebaSegmenter.SegMode.INDEX)
                .stream()
                .map(seg -> seg.word)
                .collect(Collectors.toSet());
    }

    /**
     * 计算帖子的TF-IDF向量
     * @param postContents 帖子ID与内容的映射
     * @return 帖子ID与其每个词语的TF-IDF向量的映射
     */
    private Map<Integer, Map<String, Double>> calculateTFIDF(Map<Integer, String> postContents) {
        Map<Integer, Map<String, Double>> tfidfVectors = new HashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>(); // 词语在所有文档中出现的次数
        int totalDocuments = postContents.size();

        // 计算词频和文档频率
        for (Map.Entry<Integer, String> entry : postContents.entrySet()) {
            int postId = entry.getKey();
            String content = entry.getValue();
            Set<String> tokens = tokenize(content);
            Map<String, Double> termFrequency = new HashMap<>();

            for (String token : tokens) {
                termFrequency.put(token, termFrequency.getOrDefault(token, 0.0) + 1.0);
                documentFrequency.put(token, documentFrequency.getOrDefault(token, 0) + 1); // 在所有文档中的出现次数
            }

            tfidfVectors.put(postId, termFrequency);
        }

        // 计算TF-IDF
        for (Map<String, Double> termFrequency : tfidfVectors.values()) { // 一篇文章的内词语的词频
            for (Map.Entry<String, Double> entry : termFrequency.entrySet()) { // 一个词语的词频
                String token = entry.getKey();
                double tf = entry.getValue();
                double idf = Math.log((double) totalDocuments / (1 + documentFrequency.get(token))); // 文档集总数/词语在文档集中出现的次数
                termFrequency.put(token, tf * idf); // 一个词语的TF-IDF向量值
            }
        }

        return tfidfVectors;
    }

    /**
     * 计算两个TF-IDF向量之间的余弦相似度 --- 相交的部分相乘，不相交的部分自己平方
     * @param vector1 第一个TF-IDF向量
     * @param vector2 第二个TF-IDF向量
     * @return 余弦相似度得分
     */
    private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        Set<String> allTokens = new HashSet<>(vector1.keySet());
        allTokens.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String token : allTokens) {
            double v1 = vector1.getOrDefault(token, 0.0);
            double v2 = vector2.getOrDefault(token, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        // 避免除以0的情况
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 计算帖子内容与用户交互过的帖子的内容相似度得分
     * @param targetPostId   要比较的帖子内容
     * @param basePosts 基准集
     * @return 相似度得分
     */
    private double calculateContentSimilarity(Integer targetPostId, Set<Integer> basePosts) {
        Map<String, Double> targetVector = tfidfVectors.get(targetPostId); // 目标帖子的TF-IDF向量

        double totalSimilarity = 0.0;
        for (Integer postId : basePosts) {
            Map<String, Double> userPostVector = tfidfVectors.get(postId);
            totalSimilarity += cosineSimilarity(targetVector, userPostVector);
        }

        return totalSimilarity / basePosts.size();
    }

    /**
     * 计算用户交互行为的权重
     * @param interaction 用户交互行为
     * @return 权重
     */
    private double calculateInteractionWeight(UserInteraction interaction) {
        double typeWeight = INTERACTION_TYPE_WEIGHTS.getOrDefault(interaction.getInteractionType(), 0.5);
        long timeDiff = System.currentTimeMillis() - interaction.getInteractionTime().getTime();
        double timeDecay = Math.exp(-timeDiff / (double) TIME_DECAY_FACTOR);
        return typeWeight * timeDecay;
    }
}