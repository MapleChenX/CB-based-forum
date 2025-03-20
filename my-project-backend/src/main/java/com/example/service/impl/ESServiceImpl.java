package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.alibaba.fastjson2.JSONObject;
import com.example.common.Const;
import com.example.entity.VectorInsert;
import com.example.service.ESService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ESServiceImpl implements ESService {

    @Resource
    private ElasticsearchClient client;


    @PostConstruct
    public void init() throws IOException {
        recreateIndex();
    }

    @RabbitListener(queues = Const.INSERT_VECTOR_QUEUE)
    public void insertVector(String content) {
        VectorInsert vectorInsert = JSONObject.parseObject(content, VectorInsert.class);
        try {
            if (isExists(vectorInsert.getId())) {
                System.out.println("ES中已存在该id，不插入！" + vectorInsert.getId());
                return;
            }
            insertPostWithId(vectorInsert.getId(), vectorInsert.getTitle(), vectorInsert.getContent(), vectorInsert.getVector());
            System.out.println("插入成功！" + vectorInsert.getId());
        } catch (IOException e) {
            System.out.println("insert into es wrong! id: " + vectorInsert.getId());
        }
    }

    void recreateIndex() throws IOException {
//        client.indices().delete(d -> d.index(Const.ES_INDEX_FORUM_POSTS));  // 删除索引
//        createIndex();  // 重新创建索引
        createIndexIfNotExists();  // 重新创建索引
    }

    void createIndex() throws IOException {
        boolean exists = client.indices().exists(e -> e.index(Const.ES_INDEX_FORUM_POSTS)).value();
        if (!exists) {
            client.indices().create(c -> c
                    .index(Const.ES_INDEX_FORUM_POSTS)
                    .mappings(m -> m
                            .properties("title", p -> p.text(t -> t))
                            .properties("content", p -> p.text(t -> t))
                            .properties("embedding", p -> p
                                    .denseVector(d -> d
                                            .dims(768)  // 向量维度
                                    )
                            )
                    )
            );
            System.out.println("ES索引创建成功");
        } else {
            System.out.println("ES索引已存在，无需创建");
        }
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

    public void insertPostWithId(String id, String title, String content, List<Double> vector) throws IOException {
        client.index(i -> i
                .index(Const.ES_INDEX_FORUM_POSTS)
                .id(id)
                .document(Map.of(
                        "title", title,
                        "content", content,
                        "embedding", vector
                ))
        );
    }

    public boolean isExists(String id) throws IOException {
        return client.exists(e -> e.index(Const.ES_INDEX_FORUM_POSTS).id(id)).value();
    }

}
