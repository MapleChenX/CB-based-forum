package com.example.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.ScriptScoreFunction;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.example.common.Const;
import com.example.service.ESService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ESServiceImpl implements ESService {

    @Resource
    private ElasticsearchClient client;



    void recreateIndex() throws IOException {
        client.indices().delete(d -> d.index(Const.ES_INDEX_FORUM_POSTS));  // 删除索引
        createIndex();  // 重新创建索引
//        createIndexIfNotExists();  // 重新创建索引
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

    void createInferencePipeline() throws IOException {
        client.ingest().putPipeline(p -> p
                .id("text_embedding_pipeline")
                .processors(pr -> pr
                        .inference(i -> i
                                .modelId("my_bert_model")  // 你的 BERT 模型
                                .inferenceConfig(c -> c.textEmbedding(te -> te))
                                .fieldMap(f -> f
                                        .put("content", "embedding") // content 转 embedding
                                )
                        )
                )
        );
    }

    void insertPostWithId(String id, String title, String content) throws IOException {
        client.index(i -> i
                .index("forum_posts")
                .id(id)  // 这里手动指定 ID
                .pipeline("text_embedding_pipeline")
                .document(Map.of(
                        "title", title,
                        "content", content
                ))
        );
    }

    void searchSimilarPosts(float[] queryVector) throws IOException {
        SearchResponse<Map> response = client.search(s -> s
                        .index("forum_posts")
                        .query(q -> q
                                .knn(knn -> knn
                                        .field("embedding")
                                        .queryVector(queryVector) // 你的搜索向量
                                        .k(5)
                                        .numCandidates(100)
                                )
                        ),
                Map.class
        );

        response.hits().hits().forEach(hit -> {
            System.out.println(hit.source());
        });
    }

    public void searchByPostId(String postId) throws IOException {
        // 获取指定帖子 ID 的 embedding 向量
        var getResponse = client.get(g -> g
                .index("forum_posts")
                .id(postId), Map.class);

        Map<String, Object> source = getResponse.source();
        if (source != null) {
            float[] embedding = (float[]) source.get("embedding");

            // 余弦相似度查询
            ScriptScoreFunction scriptScoreFunction = new ScriptScoreFunction.Builder()
                    .script(s -> s
                            .source("cosineSimilarity(params.queryVector, 'embedding') + 1.0")  // 计算余弦相似度
                            .params(Map.of("queryVector", JsonData.of(embedding)))  // 将查询向量传递给脚本
                    ).build();

            FunctionScoreQuery functionScoreQuery = new FunctionScoreQuery.Builder()
                    .query(QueryBuilders.matchAll())  // 查询所有文档
                    .functions(f -> f.scriptScore(scriptScoreFunction))  // 应用 script_score 函数
                    .build();

            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("forum_posts")
                    .query(Query.of(q -> q.functionScore(functionScoreQuery)))  // 应用 KNN 查询
                    .build();

            SearchResponse<Map> response = client.search(searchRequest, Map.class);

            // 输出搜索结果
            response.hits().hits().forEach(hit -> {
                System.out.println(hit.source());
            });
        }







}
