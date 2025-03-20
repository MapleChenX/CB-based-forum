package com.example.entity;

import lombok.Data;

import java.util.List;

@Data
public class VectorInsert {
    private String id;
    private String title;
    private String content;
    private List<Double> vector;

}
