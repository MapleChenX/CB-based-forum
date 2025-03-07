//package com.example.utils;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//public class TFIDFModel {
//
//    private final Map<Integer, String> postContents;
//    private final Map<Integer, Map<String, Double>> tfidfVectors;
//
//    public TFIDFModel(Map<Integer, String> postContents) {
//        this.postContents = postContents;
//        this.tfidfVectors = calculateTFIDF(postContents);
//    }
//
//    private Map<Integer, Map<String, Double>> calculateTFIDF(Map<Integer, String> postContents) {
//        Map<Integer, Map<String, Double>> tfidfVectors = new HashMap<>();
//        Map<String, Integer> documentFrequency = new HashMap<>();
//        int totalDocuments = postContents.size();
//
//        for (Map.Entry<Integer, String> entry : postContents.entrySet()) {
//            int postId = entry.getKey();
//            String content = entry.getValue();
//            Set<String> tokens = tokenize(content);
//            Map<String, Double> termFrequency = new HashMap<>();
//
//            for (String token : tokens) {
//                termFrequency.put(token, termFrequency.getOrDefault(token, 0.0) + 1.0);
//                documentFrequency.put(token, documentFrequency.getOrDefault(token, 0) + 1);
//            }
//
//            tfidfVectors.put(postId, termFrequency);
//        }
//
//        for (Map<String, Double> termFrequency : tfidfVectors.values()) {
//            for (Map.Entry<String, Double> entry : termFrequency.entrySet()) {
//                String token = entry.getKey();
//                double tf = entry.getValue();
//                double idf = Math.log((double) totalDocuments / (1 + documentFrequency.get(token)));
//                termFrequency.put(token, tf * idf);
//            }
//        }
//
//        return tfidfVectors;
//    }
//}
