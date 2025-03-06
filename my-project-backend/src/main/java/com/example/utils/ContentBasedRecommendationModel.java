package com.example.utils;

import com.example.entity.UserInteraction;
import com.huaban.analysis.jieba.JiebaSegmenter;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContentBasedRecommendationModel {

    private final JiebaSegmenter segmenter = new JiebaSegmenter();

    public static final List<UserInteraction> interactions = new ArrayList<>();

    private static final int TOP_N = 10; // 返回的推荐数量

    private static final long TIME_DECAY_FACTOR = 1000 * 60 * 60 * 24 * 30L; // 30 days

    private static final Map<String, Double> INTERACTION_TYPE_WEIGHTS = Map.of(
        "view", 0.6,
        "comment", 0.8,
        "like", 1.0,
        "collect", 1.2
    );

    @PostConstruct
    public void init() {
        updateInteractions();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // 每1小时执行一次
    public void updateInteractions() {
        // todo 从数据库中读取用户交互数据
    }

    /**
     * 根据帖子内容推荐帖子
     * @param userId 用户ID
     * @param topicId 帖子ID
     *  interactions 用户交互数据列表
     *  postContents 帖子内容映射
     * @return 推荐的帖子ID列表
     */
    public List<Integer> recommendPosts(int userId, Integer topicId) {
        // todo 填充interactions和postContents
        List<UserInteraction> interactions = null;
        Map<Integer, String> postContents = new HashMap<>();

        Set<Integer> userPosts = new HashSet<>();
        Map<Integer, Double> postWeights = new HashMap<>();

        for (UserInteraction interaction : interactions) {
            if (interaction.getUserId() == userId) {
                userPosts.add(interaction.getPostId());
                postWeights.put(interaction.getPostId(), postWeights.getOrDefault(interaction.getPostId(), 0.0) + calculateInteractionWeight(interaction));
            }
        }

        Map<Integer, Double> postScores = new HashMap<>();
        for (Integer postId : postContents.keySet()) {
            if (!userPosts.contains(postId)) {
                double score = calculateContentSimilarity(postContents.get(postId), userPosts, postContents);
                postScores.put(postId, score);
            }
        }

        List<Map.Entry<Integer, Double>> sortedPosts = new ArrayList<>(postScores.entrySet());
        sortedPosts.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Integer> recommendedPosts = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : sortedPosts) {
            recommendedPosts.add(entry.getKey());
        }

        return recommendedPosts.subList(0, Math.min(TOP_N, recommendedPosts.size()));
    }


    /**
     * 对帖子的内容进行分词
     * @param content 要分词的内容
     * @return 分词后的集合
     */
    private Set<String> tokenize(String content) {
        return segmenter.process(content, JiebaSegmenter.SegMode.INDEX)
                .stream()
                .map(seg -> seg.word)
                .collect(Collectors.toSet());
    }

    /**
     * 计算帖子的TF-IDF向量
     * @param postContents 帖子ID与内容的映射
     * @return 帖子ID与其TF-IDF向量的映射
     */
    private Map<Integer, Map<String, Double>> calculateTFIDF(Map<Integer, String> postContents) {
        Map<Integer, Map<String, Double>> tfidfVectors = new HashMap<>();
        Map<String, Integer> documentFrequency = new HashMap<>();
        int totalDocuments = postContents.size();

        // 计算词频和文档频率
        for (Map.Entry<Integer, String> entry : postContents.entrySet()) {
            int postId = entry.getKey();
            String content = entry.getValue();
            Set<String> tokens = tokenize(content);
            Map<String, Double> termFrequency = new HashMap<>();

            for (String token : tokens) {
                termFrequency.put(token, termFrequency.getOrDefault(token, 0.0) + 1.0);
                documentFrequency.put(token, documentFrequency.getOrDefault(token, 0) + 1);
            }

            tfidfVectors.put(postId, termFrequency);
        }

        // 计算TF-IDF
        for (Map<String, Double> termFrequency : tfidfVectors.values()) {
            for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
                String token = entry.getKey();
                double tf = entry.getValue();
                double idf = Math.log((double) totalDocuments / (1 + documentFrequency.get(token)));
                termFrequency.put(token, tf * idf);
            }
        }

        return tfidfVectors;
    }

    /**
     * 计算两个TF-IDF向量之间的余弦相似度
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
     * @param content 要比较的帖子内容
     * @param userPosts 用户交互过的帖子ID集合
     * @param postContents 帖子ID与内容的映射
     * @return 相似度得分
     */
    private double calculateContentSimilarity(String content, Set<Integer> userPosts, Map<Integer, String> postContents) {
        Map<Integer, Map<String, Double>> tfidfVectors = calculateTFIDF(postContents);
        Map<String, Double> targetVector = calculateTFIDF(Map.of(-1, content)).get(-1);

        double totalSimilarity = 0.0;
        for (Integer postId : userPosts) {
            Map<String, Double> userPostVector = tfidfVectors.get(postId);
            totalSimilarity += cosineSimilarity(targetVector, userPostVector);
        }

        return totalSimilarity / userPosts.size();
    }

    private double calculateInteractionWeight(UserInteraction interaction) {
        double typeWeight = INTERACTION_TYPE_WEIGHTS.getOrDefault(interaction.getInteractionType(), 0.5);
        long timeDiff = System.currentTimeMillis() - interaction.getInteractionTime().getTime();
        double timeDecay = Math.exp(-timeDiff / (double) TIME_DECAY_FACTOR);
        return typeWeight * timeDecay;
    }
}