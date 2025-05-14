package com.example.entity;

import lombok.Data;

import java.util.List;

@Data
public class ESPostVector {
    private Integer id;
    private String title;
    private String content;
    private List<Double> embedding;
}
