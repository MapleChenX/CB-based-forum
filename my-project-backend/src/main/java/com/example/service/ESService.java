package com.example.service;

import java.io.IOException;
import java.util.List;

public interface ESService {
    public void insertPostWithId(String id, String title, String content, List<Double> vector);
    public List<String> getSimilarPostsById(String id);
}
