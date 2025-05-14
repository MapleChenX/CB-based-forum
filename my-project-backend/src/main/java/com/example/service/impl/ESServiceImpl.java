package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.SearchTemplateRequest;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson2.JSONObject;
import com.example.common.Const;
import com.example.entity.ESPostVector;
import com.example.service.ESService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ESServiceImpl implements ESService {

    // indices index get search update delete

    @Resource
    private ElasticsearchClient client;


    @PostConstruct
    public void init() throws IOException {
        recreateIndex();
    }

    @RabbitListener(queues = Const.INSERT_VECTOR_QUEUE)
    public void insertVector(String content) {
        ESPostVector ESPostVector = JSONObject.parseObject(content, ESPostVector.class);
        try {
            if (isExists(String.valueOf(ESPostVector.getId()))) {
                System.out.println("ES中已存在该id，不插入！" + ESPostVector.getId());
                return;
            }
            insertPostWithId(String.valueOf(ESPostVector.getId()), ESPostVector.getTitle(), ESPostVector.getContent(), ESPostVector.getEmbedding());
        } catch (IOException e) {
            System.out.println("insert into es wrong! id: " + ESPostVector.getId());
        }
    }

    @RabbitListener(queues = Const.POSTS_DEL_2_ES_MQ)
    public void deletePost(String id) {
        try {
            client.delete(d -> d
                    .index(Const.ES_INDEX_FORUM_POSTS)
                    .id(id)
            );
            System.out.println("ES删除成功，id：" + id);
        } catch (Exception e) {
            System.out.println("ES删除失败，id：" + id);
        }
    }

    void recreateIndex() throws IOException {
        createIndexIfNotExists();  // 重新创建索引
    }

    void createIndexIfNotExists() throws IOException {
        boolean exists = client.indices().exists(e -> e.index(Const.ES_INDEX_FORUM_POSTS)).value();
        if (!exists) {
            client.indices().create(c -> c
                    .index(Const.ES_INDEX_FORUM_POSTS)
                    .mappings(m -> m
                            .properties("title", p -> p.text(t -> t))
                            .properties("content", p -> p.text(t -> t))
                            .properties("embedding", p -> p
                                    .denseVector(d -> d
                                            .dims(768)
                                            .index(true)
                                            .similarity("cosine")
                                    )
                            )
                    )
            );
            System.out.println("ES索引创建成功");
        } else {
            System.out.println("ES索引已存在，无需创建");
        }
    }

    public void insertPostWithId(String id, String title, String content, List<Double> vector)  {
        try {
            client.index(i -> i
                    .index(Const.ES_INDEX_FORUM_POSTS)
                    .id(id)
                    .document(Map.of(
                            "title", title,
                            "content", content,
                            "embedding", vector
                    ))
            );
            System.out.println("ES插入成功，id：" + id);
        }catch (Exception e) {
            System.out.println("插入ES失败：" + e.getMessage());
        }
    }

    public void insertPostWithId(ESPostVector one)  {
        try {
            client.index(i -> i
                    .index(Const.ES_INDEX_FORUM_POSTS)
                    .id(String.valueOf(one.getId()))
                    .document(one)
            );
            System.out.println("ES插入成功，id：" + one.getId());
        }catch (Exception e) {
            System.out.println("插入ES失败：" + e.getMessage());
        }
    }


    @Override
    public List<String> getSimilarPostsById(String id) {
        try {
            // 获取向量
            GetResponse<ESPostVector> response = client.get(g -> g
                            .index(Const.ES_INDEX_FORUM_POSTS)
                            .id(id),
                    ESPostVector.class
            );

            if (!response.found()) {
                return null;
            }

            List<Double> vector = response.source().getEmbedding();

            // 相似向量查询
            SearchResponse<ESPostVector> resp = client.search(s -> s
                            .index(Const.ES_INDEX_FORUM_POSTS)
                            .knn(k -> k
                                    .field("embedding")
                                    .queryVector(vector)
                                    .k(10)
                                    .numCandidates(100)
                            )
                            .size(10),
                    ESPostVector.class
            );

            return resp.hits().hits().stream()
                    .map(Hit::id)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isExists(String id) throws IOException {
        return client.exists(e -> e.index(Const.ES_INDEX_FORUM_POSTS).id(id)).value();
    }

}
