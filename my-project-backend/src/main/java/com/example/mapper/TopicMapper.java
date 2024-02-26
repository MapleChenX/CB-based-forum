package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dto.Interact;
import com.example.entity.dto.Topic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
    // 动态拼接sql
    @Insert("""
            <script>
                insert ignore into db_topic_interact_${type} values
                <foreach collection ="interacts" item="item" separator =",">
                    (#{item.tid}, #{item.uid}, #{item.time})
                </foreach>
            </script>
            """)
    void addInteract(List<Interact> interacts, String type);

    @Delete("""
            <script>
                delete from db_topic_interact_${type} where
                <foreach collection="interacts" item="item" separator=" or ">
                    (tid = #{item.tid} and uid = #{item.uid})
                </foreach>
            </script>
            """)
    int deleteInteract(List<Interact> interacts, String type);

    // ${}是直接替换，用于非查询字段；#{}是预编译，在sql中先是?，然后再替换，防止sql注入，用于查询字段

    // 当前帖子的交互数
    @Select("""
            select count(*) from db_topic_interact_${type} where tid = #{tid}
            """)
    int interactCount(int tid, String type);

    // 当前用户与帖子的交互状态
    @Select("""
            select count(*) from db_topic_interact_${type} where tid = #{tid} and uid = #{uid}
            """)
    int userInteractCount(int tid, int uid, String type);

    @Select("""
            select * from db_topic_interact_collect left join db_topic on tid = db_topic.id
             where db_topic_interact_collect.uid = #{uid}
            """)
    List<Topic> collectTopics(int uid);
}
