package com.example.service;

import com.example.entity.dto.Interact;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InteractService {
    List<Interact> getAllLikes();

    List<Interact> getAllCollects();

    List<Interact> getAllInteractions();

    List<Interact> getUserLikes(Integer uid);

    List<Interact> getUserCollects(Integer uid);

    List<Interact> getUserInteractions(Integer uid);


}
