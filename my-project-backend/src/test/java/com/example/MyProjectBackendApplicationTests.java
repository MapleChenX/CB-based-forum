//package com.example;
//
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.example.entity.dto.Topic;
//import com.example.mapper.TopicMapper;
//import com.huaban.analysis.jieba.JiebaSegmenter;
//import jakarta.annotation.Resource;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import java.util.List;
//
//@SpringBootTest
//class MyProjectBackendApplicationTests {
//
//    @Resource
//    TopicMapper mapper;
//
//    @Test
//    void contextLoads() {
//        System.out.println(new BCryptPasswordEncoder().encode("123456"));
//
//    }
//
//    @Test
//    void contextLoads1() {
//        // 创建Page对象
//        Page<Topic> page = Page.of(1, 10);
//        mapper.selectPage(page, Wrappers.<Topic>query().orderByDesc("time"));
//        List<Topic> records = page.getRecords();
//        records.forEach(System.out::println);
//
//    }
//
//    @Test
//    public void testJiebaSegmenter() {
//        JiebaSegmenter segmenter = new JiebaSegmenter();
//        String text = "我来到北京清华大学, so, what's up? nice to meet u everybody!";
//        List<String> segmentedText = segmenter.sentenceProcess(text);
//        System.out.println(segmentedText);
//    }
//}
