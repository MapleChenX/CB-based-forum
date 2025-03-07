package com.example.mapper;

import com.example.entity.dto.Interact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InteractMapper {

    @Select("SELECT * FROM db_topic_interact_like")
    List<Interact> getAllLikes();

    @Select("SELECT * FROM db_topic_interact_collect")
    List<Interact> getAllCollects();

    @Select("SELECT * FROM db_topic_interact_like WHERE uid = #{uid}")
    List<Interact> getUserLikes(Integer uid);

    @Select("SELECT * FROM db_topic_interact_collect WHERE uid = #{uid}")
    List<Interact> getUserCollects(Integer uid);

}
