package com.example.tfidf;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.example.utils.ContentBasedRecommendationModel.tfidfVectors;

public class ExportTFIDF {
    public static void main(String[] args) throws IOException {
        // 1️⃣ 构建全局词汇表（保证所有帖子向量维度一致）
        Set<String> globalVocabulary = new TreeSet<>();
        for (Map<String, Double> vector : tfidfVectors.values()) {
            globalVocabulary.addAll(vector.keySet());
        }
        List<String> vocabList = new ArrayList<>(globalVocabulary);  // 确保顺序固定

        // 2️⃣ 写入 CSV，保证所有帖子向量维度相同
        FileWriter writer = new FileWriter("tfidf_vectors.csv");

        for (Map.Entry<Integer, Map<String, Double>> entry : tfidfVectors.entrySet()) {
            int docId = entry.getKey();
            Map<String, Double> vector = entry.getValue();

            // 写入 docId
            writer.write(docId + ",");

            // 按 vocabList 顺序写入 TF-IDF 值，确保维度固定
            for (String word : vocabList) {
                writer.write(vector.getOrDefault(word, 0.0) + ",");
            }
            writer.write("\n");
        }
        writer.close();
        System.out.println("TF-IDF 数据已导出！");
    }
}

