package com.example.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import jakarta.annotation.Resource;
import org.checkerframework.checker.units.qual.K;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CacheUtils {
    @Resource
    StringRedisTemplate template;

    public <T> T takeFromCache(String key, Class<T> dataType) {
        String s = template.opsForValue().get(key);
        if(s == null) return null;
        return JSONObject.parseObject(s).to(dataType);
    }

    public <T> List<T> takeListFromCache(String key, Class<T> itemType) {
        String s = template.opsForValue().get(key);
        if(s == null) return null;
        return JSONArray.parseArray(s).toList(itemType);
    }

    public <T> void saveToCache(String key, T data, long expire) {
        template.opsForValue().set(key, JSONObject.from(data).toJSONString(), expire, TimeUnit.SECONDS);
    }

    public <T> void saveListToCache(String key, List<T> list, long expire) {
        template.opsForValue().set(key, JSONArray.from(list).toJSONString(), expire, TimeUnit.SECONDS);
    }

    public void deleteCachePattern(String key){
        Set<String> keys = Optional.ofNullable(template.keys(key)).orElse(Collections.emptySet());
        template.delete(keys);
    }

    public void deleteCache(String key){
        template.delete(key);
    }


    public String getFromHashBucketById(String bucket, Integer postId) {
        return Objects.requireNonNull(template.opsForHash().get(bucket, postId.toString())).toString();
    }

    public void save2HashBucketById(String bucket, Integer postId, String content) {
        template.opsForHash().put(bucket, postId.toString(), content);
    }

    public Map<Object, Object> getAllByHashBucket(String bucket) {
        return template.opsForHash().entries(bucket);
    }

    public Cursor<Map.Entry<Object, Object>> getIteratorByHashBucket(String bucket) {
        return template.opsForHash().scan(bucket, ScanOptions.scanOptions().match("*").count(100).build());
    }

    public <K, V> Cursor<Map.Entry<K, V>> getRedisIterator(String bucket, TypeReference<K> keyTypeReference, TypeReference<V> valueTypeReference) {
        ScanOptions scanOptions = ScanOptions.scanOptions().count(500).match("*").build();
        Cursor<Map.Entry<Object, Object>> cursor = template.opsForHash().scan(bucket, scanOptions);

        return new Cursor<Map.Entry<K, V>>() {

            @Override
            public long getCursorId() {
                return cursor.getCursorId();
            }

            @Override
            public boolean isClosed() {
                return cursor.isClosed();
            }

            @Override
            public long getPosition() {
                return cursor.getPosition();
            }

            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }

            @Override
            public Map.Entry<K, V> next() {
                Map.Entry<Object, Object> entry = cursor.next();
                K key = JSON.parseObject(String.valueOf(entry.getKey()), keyTypeReference);
                V value = JSON.parseObject(String.valueOf(entry.getValue()), valueTypeReference);
                return new Map.Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return key;
                    }

                    @Override
                    public V getValue() {
                        return value;
                    }

                    @Override
                    public V setValue(V value) {
                        throw new UnsupportedOperationException("setValue operation is not supported.");
                    }
                };
            }

            @Override
            public void close() {
                cursor.close();
            }
        };
    }



}
