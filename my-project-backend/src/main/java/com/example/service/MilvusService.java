//package com.example.service;
//
//import io.milvus.client.MilvusServiceClient;
//import io.milvus.grpc.*;
//import io.milvus.param.MetricType;
//import io.milvus.param.R;
//import io.milvus.param.collection.FieldType;
//import io.milvus.param.collection.CreateCollectionParam;
//import io.milvus.param.dml.*;
//import io.milvus.param.dml.SearchParam;
//import io.milvus.param.dml.InsertParam;
//import io.milvus.param.dml.InsertParam.Field;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class MilvusService {
//    private static final String COLLECTION_NAME = "tfidf_vectors"; // 表名
//    private static final String ID_FIELD = "id"; // ID 字段
//    private static final String VECTOR_FIELD = "vector"; // 向量字段,存储的是每个词语的TF-IDF值
//    private static final int VECTOR_DIM = 768; // 向量维度 (示例)
//
//    private final MilvusServiceClient client;
//
//    @Autowired
//    public MilvusService(MilvusServiceClient client) {
//        this.client = client;
//        createCollection(); // 启动时自动创建 Collection
//    }
//
//    /**
//     * 创建 Milvus Collection（向量表）
//     */
//    private void createCollection() {
//        client.createCollection(CreateCollectionParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withShardsNum(2)
//                .addFieldType(FieldType.newBuilder().withName(ID_FIELD).withDataType(DataType.Int64)
//                        .withPrimaryKey(true).withAutoID(true).build())
//                .addFieldType(FieldType.newBuilder().withName(VECTOR_FIELD).withDataType(DataType.FloatVector)
//                        .withDimension(VECTOR_DIM).build())
//                .build());
//    }
//
//    /**
//     * 插入向量
//     */
//    public void insertVector(Integer id, List<Float> vector) {
//        String collectionName = "tfidf_vectors";
//
//        // 1️⃣ 创建 ID 字段（如果 Milvus 不使用 AutoID）
//        Field idField = new Field("id", Collections.singletonList(id));
//
//        // 2️⃣ 创建向量字段
//        Field vectorField = new Field("vector", Collections.singletonList(vector));
//
//        // 3️⃣ 组合 ID 和 向量一起插入
//        InsertParam insertParam = InsertParam.newBuilder()
//                .withCollectionName(collectionName)
//                .withFields(Arrays.asList(idField, vectorField)) // ✅ 需要的是 List<Field>
//                .build();
//
//        client.insert(insertParam);
//    }
//
//    /**
//     * 更新向量（Milvus 不支持直接更新，需要删除后重新插入）
//     */
//    public void updateVector(Integer id, List<Float> vector) {
//        deleteVector(id);
//        insertVector(id, vector);
//    }
//
//    /**
//     * 查询向量
//     */
//    public List<Float> getVectorById(Integer id) {
//        QueryParam queryParam = QueryParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withExpr(ID_FIELD + " == " + id)
//                .withOutFields(Collections.singletonList(VECTOR_FIELD))
//                .build();
//
//        R<QueryResults> queryResult = client.query(queryParam);
//
//
//        return null;
//    }
//
//    /**
//     * 删除向量
//     */
//    public void deleteVector(Integer id) {
//        client.delete(DeleteParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withExpr(ID_FIELD + " == " + id)
//                .build());
//    }
//
//    /**
//     * 分页查询相似的帖子
//     */
//    public List<Long> searchSimilarVectors(Integer id, int offset, int pageSize) {
//        List<Float> queryVector = getVectorById(id);
//        if (queryVector == null) {
//            return Collections.emptyList();
//        }
//
//        SearchParam searchParam = SearchParam.newBuilder()
//                .withCollectionName(COLLECTION_NAME)
//                .withTopK(pageSize + offset)
//                .withVectors(Collections.singletonList(queryVector))
//                .withMetricType(MetricType.L2) // 余弦相似度可用 IP（Inner Product）
//                .build();
//
//        List<SearchResultData> results = client.search(searchParam).getData();
//        return results.stream()
//                .map(result -> (Long) result.getIds().getIdArrayList().get(0))
//                .skip(offset)
//                .limit(pageSize)
//                .collect(Collectors.toList());
//    }
//}
//
