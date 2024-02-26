package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class Interact {
    Integer tid;
    Integer uid;
    Date time;
    String type;

    // 做键
    public String toKey() {
        return tid + ":" + uid;
    }

    // 解析键
    public static Interact parseInteract(String str, String type){
        String[] keys = str.split(":");
        return new Interact(Integer.parseInt(keys[0]), Integer.parseInt(keys[1]), new Date(), type);
    }
}
