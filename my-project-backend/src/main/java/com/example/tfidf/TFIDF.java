package com.example.tfidf;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.example.utils.CacheUtils;
import com.example.common.Const;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TFIDF {


    /** 计算、存储TFIDF、获取
     * 数据结构1：tfidfVectors
     * 数据结构2：content k-v
     */

    @Resource
    StringRedisTemplate template;

    @Resource
    private CacheUtils cacheUtils;


    /**
     * 新增 8 个方法：
     *  content：删除、修改、查询、迭代
     *  tfidfVectors：删除、修改、查询、迭代
     */

    public void saveContents2Redis(Map<Integer, String> postContents) {
        Map<String, String> contentMap = new HashMap<>();
        postContents.forEach((postId, content) -> contentMap.put(postId.toString(), content));
        template.opsForHash().putAll(Const.POST_CONTENT_BUCKET, contentMap);
    }

    public void saveTFIDFs2Redis(Map<Integer, Map<String, Double>> tfidfVectors) {
        Map<String, String> tfidfMap = new HashMap<>();
        tfidfVectors.forEach((postId, vector) -> {
            String jsonVector = JSON.toJSONString(vector);
            tfidfMap.put(postId.toString(), jsonVector);
        });
        template.opsForHash().putAll(Const.TFIDF_BUCKET, tfidfMap);
    }

    public void sync2Redis(Map<Integer, String> postContents, Map<Integer, Map<String, Double>> tfidfVectors) {
        saveContents2Redis(postContents);
        saveTFIDFs2Redis(tfidfVectors);
    }

    public String getContentFromHashBucketById(Integer postId) {
        return Objects.requireNonNull(template.opsForHash().get(Const.POST_CONTENT_BUCKET, postId.toString())).toString();
    }

    public Map<String, Double> getTFIDFFromHashBucketById(Integer postId) {
        String jsonVector = (String) template.opsForHash().get(Const.TFIDF_BUCKET, postId.toString());
        if (jsonVector == null) {
            return Collections.emptyMap();  // 或者返回 null，根据需求
        }
        return JSON.parseObject(jsonVector, new TypeReference<Map<String, Double>>() {});
    }

}
