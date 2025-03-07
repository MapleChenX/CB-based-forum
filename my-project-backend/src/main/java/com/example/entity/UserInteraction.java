package com.example.entity;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

@Data
public class UserInteraction {
    int userId;
    int postId;
    String interactionType;
    Date interactionTime;
}
