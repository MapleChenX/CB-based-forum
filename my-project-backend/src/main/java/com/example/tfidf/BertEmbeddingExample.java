//package com.example.tfidf;
//
//
//import ai.djl.Model;
//import ai.djl.ModelException;
//import ai.djl.inference.Predictor;
//import ai.djl.modality.nlp.bert.BertTokenizer;
//import ai.djl.translate.TranslateException;
//import ai.djl.translate.Translator;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//public class BertEmbeddingExample {
//    public static void main(String[] args) throws IOException, ModelException, TranslateException {
//        // 加载预训练 BERT 模型
//        String modelName = "ai.djl.huggingface.pytorch:bert-base-uncased";
//        Model model = Model.newInstance(modelName);
//
//        // 处理输入文本
//        String text = "这是一个测试帖子，讨论电影推荐";
//        Translator<String, float[]> translator = new BertWordEmbeddingTranslator();
//        Predictor<String, float[]> predictor = model.newPredictor(translator);
//
//        float[] embedding = predictor.predict(text);
//        System.out.println(Arrays.toString(embedding)); // 输出 BERT 生成的向量
//    }
//}
