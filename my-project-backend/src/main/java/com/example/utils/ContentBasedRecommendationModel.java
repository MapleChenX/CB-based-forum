package com.example.utils;

import com.example.common.VectorMap;
import com.example.entity.UserInteraction;
import com.example.service.InteractService;
import com.example.service.TopicService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaban.analysis.jieba.JiebaSegmenter;
import jakarta.annotation.Resource;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ContentBasedRecommendationModel implements ApplicationRunner {

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
    private RabbitMQUtil rabbitMQUtil;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        updateTFIDF();
    }

    public void updateTFIDF() {
        WebClient webClient = WebClient.create("http://127.0.0.1:8000");
        long start = System.currentTimeMillis();
        System.out.println("Updating tfidfVectors...");
        postContents.clear();

        topicService.list().forEach(topic -> {
            String optimizePostContent = optimizePostContent(topic.getContent());
            String title = topic.getTitle();
            postContents.put(topic.getId(), optimizePostContent);

//            Map<String, String> request = new HashMap<>();
//            request.put("title", title);
//            request.put("content", optimizePostContent);
//
//            text2vectorResp resp = webClient.post()
//                    .uri("/text2vector")
//                    .bodyValue(request)
//                    .retrieve()
//                    .bodyToMono(text2vectorResp.class)
//                    .block();
//
//            ESPostVector vectorInsert = new ESPostVector();
//            vectorInsert.setId(topic.getId().toString());
//            vectorInsert.setTitle(title);
//            vectorInsert.setContent(optimizePostContent);
//            if (resp != null) {
//                vectorInsert.setVector(resp.getVector());
//            }
//            rabbitMQUtil.sendMessage(Const.INSERT_VECTOR_QUEUE, JSONObject.toJSONString(vectorInsert));
//            System.out.println("send to mq successfully!" + topic.getId());
        });

        tfidfVectors = calculateTFIDF(ContentBasedRecommendationModel.postContents);

        // 导出tfidf向量准备降维
//        try {
//            System.out.println("开始导出！");
////            exportTFIDF();
//            exportTFIDFToParquet("tfidf_vectors.parquet");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // 同步到redis
//        boolean hasPostContent = Boolean.TRUE.equals(template.hasKey(Const.POST_CONTENT_BUCKET))
//                && template.opsForHash().size(Const.POST_CONTENT_BUCKET) > 0;
//        boolean hasTFIDF = Boolean.TRUE.equals(template.hasKey(Const.TFIDF_BUCKET))
//                && template.opsForHash().size(Const.TFIDF_BUCKET) > 0;
//        if (hasPostContent && hasTFIDF) {
//            return;
//        }
//        tfidf.sync2Redis(postContents, tfidfVectors);

        System.out.println("tfidfVectors updated successfully!");
        System.out.println("time elapsed: " + (System.currentTimeMillis() - start) / 60000 + " minutes");
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

        VectorMap.VectorMap = documentFrequency;
        // 向量维度
        System.out.println("向量维度：" + VectorMap.count());

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
    public void exportTFIDF() throws IOException {
        // 1️⃣ 构建全局词汇表（保证所有帖子向量维度一致）
        Set<String> globalVocabulary = new TreeSet<>();
        for (Map<String, Double> vector : tfidfVectors.values()) {
            globalVocabulary.addAll(vector.keySet());
        }
        List<String> vocabList = new ArrayList<>(globalVocabulary);  // 确保顺序固定

        // 2️⃣ 使用 BufferedWriter 提高写入效率
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("tfidf_vectors.csv"))) {
            String header = String.join(",", vocabList);
            writer.write("docId," + header + "\n");

            // 3️⃣ 使用批量写入方法
            StringBuilder sb = new StringBuilder();
            int count = 0;  // 记录当前累积的行数

            for (Map.Entry<Integer, Map<String, Double>> entry : tfidfVectors.entrySet()) {
                int docId = entry.getKey();
                Map<String, Double> vector = entry.getValue();

                sb.append(docId).append(",");

                // 按 vocabList 顺序写入 TF-IDF 值，确保维度固定
                for (String word : vocabList) {
                    sb.append(vector.getOrDefault(word, 0.0)).append(",");
                }

                sb.append("\n");
                count++;

                // 每 500 条批量写入一次
                if (count % 100 == 0) {
                    writer.write(sb.toString());
                    writer.flush();
                    sb.setLength(0); // 清空 StringBuilder
                    System.out.println("批量写入 100 条数据！");
                }
            }

            // 最后剩余的数据
            if (sb.length() > 0) {
                writer.write(sb.toString());
                writer.flush();
            }

            System.out.println("TF-IDF 数据已全部导出！");
        }
    }


    public void exportTFIDFToParquet(String outputFile) throws IOException {
        // 1️⃣ 构建全局词汇表（保证所有帖子向量维度一致）
        Set<String> globalVocabulary = new TreeSet<>();
        for (Map<String, Double> vector : tfidfVectors.values()) {
            globalVocabulary.addAll(vector.keySet());
        }
        List<String> vocabList = new ArrayList<>(globalVocabulary);

        // 2️⃣ 定义 Avro Schema
        StringBuilder schemaStr = new StringBuilder("{ \"type\": \"record\", \"name\": \"TFIDFRecord\", \"fields\": [");
        schemaStr.append("{\"name\": \"docId\", \"type\": \"int\"},");
        for (String word : vocabList) {
            schemaStr.append("{\"name\": \"").append(word).append("\", \"type\": \"float\"},");
        }
        schemaStr.deleteCharAt(schemaStr.length() - 1);  // 删除最后一个逗号
        schemaStr.append("]}");

        Schema schema = new Schema.Parser().parse(schemaStr.toString());

        // 3️⃣ 创建 Parquet Writer
        Path path = Paths.get(outputFile);
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(new org.apache.hadoop.fs.Path(path.toString()))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)  // 使用 Snappy 压缩
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build()) {

            // 4️⃣ 写入数据
            for (Map.Entry<Integer, Map<String, Double>> entry : tfidfVectors.entrySet()) {
                int docId = entry.getKey();
                Map<String, Double> vector = entry.getValue();

                GenericRecord record = new GenericData.Record(schema);
                record.put("docId", docId);

                for (String word : vocabList) {
                    record.put(word, vector.getOrDefault(word, 0.0).floatValue());  // 转为 float 存储
                }

                writer.write(record);
            }
        }

        System.out.println("✅ TF-IDF 数据已导出到 Parquet 文件：" + outputFile);
    }

}