package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.alibaba.fastjson2.JSONObject;
import com.example.common.Const;
import com.example.entity.ESPostVector;
import com.example.service.ESService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        ESPostVector ESPostVector = JSONObject.parseObject(content, ESPostVector.class);
        try {
            if (isExists(ESPostVector.getId())) {
                System.out.println("ES中已存在该id，不插入！" + ESPostVector.getId());
                return;
            }
            insertPostWithId(ESPostVector.getId(), ESPostVector.getTitle(), ESPostVector.getContent(), ESPostVector.getEmbedding());
        } catch (IOException e) {
            System.out.println("insert into es wrong! id: " + ESPostVector.getId());
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

    @Override
    public List<String> getSimilarPostsById(String id) {
        // 获取指定 ID 的帖子向量
        GetRequest req = new GetRequest.Builder().index(Const.ES_INDEX_FORUM_POSTS).id(id).build();
        try {
            GetResponse<ESPostVector> esPostVectorGetResponse = client.get(req, ESPostVector.class);
            if (esPostVectorGetResponse.source() == null)
                return null;
            List<Double> vector = esPostVectorGetResponse.source().getEmbedding();
            if (vector == null || vector.isEmpty())
                return null;

            // 执行 script_score 查询计算余弦相似度
            SearchResponse<ESPostVector> response = client.search(s -> s
                            .index(Const.ES_INDEX_FORUM_POSTS)
                            .query(q -> q
                                    .scriptScore(ss -> ss
                                            .query(qq -> qq.matchAll(m -> m)) // 先匹配所有帖子
                                            .script(script -> script
                                                    .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")
                                                    .params("queryVector", vector)
                                            )
                                    )
                            )
                            .size(10)
                            .sourceFields("id"),
                            ESPostVector.class
            );

            return response.hits().hits().stream()
                    .map(hit -> {
                        if (hit.source() == null) {
                            return null;
                        }
                        return hit.source().getId();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean isExists(String id) throws IOException {
        return client.exists(e -> e.index(Const.ES_INDEX_FORUM_POSTS).id(id)).value();
    }

}
