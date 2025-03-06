package com.example.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserInteraction {
    int userId;
    int postId;
    String interactionType;
    Timestamp interactionTime;
}
