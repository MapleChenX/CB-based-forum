package com.example.mapper;

import com.example.entity.dto.Interact;
import com.example.entity.dto.InteractDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InteractMapper {

    @Select("SELECT * FROM db_topic_interact_like")
    List<Interact> getAllLikes();

    @Select("SELECT * FROM db_topic_interact_collect")
    List<Interact> getAllCollects();

    @Select("SELECT tid, uid, time FROM db_topic_interact_like WHERE uid = #{uid}")
    List<InteractDTO> getUserLikes(Integer uid);

    @Select("SELECT tid, uid, time FROM db_topic_interact_collect WHERE uid = #{uid}")
    List<InteractDTO> getUserCollects(Integer uid);

}
