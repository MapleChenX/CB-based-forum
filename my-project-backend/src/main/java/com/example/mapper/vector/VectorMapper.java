package com.example.mapper.vector;


import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VectorMapper {
    @Insert("INSERT INTO tfidf (tfidf) VALUES (#{vector})")
    void insertVector(@Param("vector") String vector);

    @Update("UPDATE tfidf SET tfidf = #{vector} WHERE id = #{id}")
    void updateVector(@Param("id") Integer id, @Param("vector") String vector);

    @Select("SELECT tfidf FROM tfidf WHERE id = #{id}")
    String getVectorById(@Param("id") Integer id);

    @Delete("DELETE FROM tfidf WHERE id = #{id}")
    void deleteVector(@Param("id") Integer id);

    /**
     * 分页查询相似的帖子
     * @param id       帖子id
     * @param offset   起始位置
     * @param pageSize 分页大小
     * @return 相似帖子ids
     */
    @Select("SELECT target.id " +
            "FROM tfidf target " +
            "WHERE target.id != #{id} " +
            "ORDER BY (SELECT cur.tfidf <#> target.tfidf FROM tfidf cur WHERE cur.id = #{id}) " +
            "LIMIT #{pageSize} OFFSET #{offset}")
    List<Integer> searchSimilarVectors(@Param("id") Integer id, @Param("offset") int offset, @Param("pageSize") int pageSize);


}

