package com.example.service.impl;

import com.example.entity.dto.Interact;
import com.example.mapper.InteractMapper;
import com.example.service.InteractService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InteractServiceImpl implements InteractService {

    @Resource
    private InteractMapper interactMapper;

    @Override
    public List<Interact> getAllLikes() {
        return interactMapper.getAllLikes().stream()
                .peek(e -> e.setType("like"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interact> getAllCollects() {
        return interactMapper.getAllCollects().stream()
                .peek(e -> e.setType("collect"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interact> getAllInteractions() {
        ArrayList<Interact> allInteractions = new ArrayList<>();
        allInteractions.addAll(getAllLikes());
        allInteractions.addAll(getAllCollects());
        return allInteractions;
    }

    @Override
    public List<Interact> getUserLikes(Integer uid) {
        return interactMapper.getUserLikes(uid).stream()
                .map(e -> new Interact(e.getTid(), e.getUid(), e.getTime(), "like"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interact> getUserCollects(Integer uid) {
        return interactMapper.getUserCollects(uid).stream()
                .map(e -> new Interact(e.getTid(), e.getUid(), e.getTime(), "collect"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Interact> getUserInteractions(Integer uid) {
        ArrayList<Interact> userInteractions = new ArrayList<>();
        userInteractions.addAll(getUserLikes(uid));
        userInteractions.addAll(getUserCollects(uid));
        return userInteractions;
    }
}
