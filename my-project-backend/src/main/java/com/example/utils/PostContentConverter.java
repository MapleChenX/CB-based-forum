package com.example.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class PostContentConverter {

    public static String convert(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // 将字符串解析为 JSON 对象
        JSONObject jsonObject = JSONObject.parseObject(content);
        // 获取 ops 数组
        JSONArray ops = jsonObject.getJSONArray("ops");
        if (ops == null) {
            return content; // 传进来的content就是普通的文本
        }

        StringBuilder text = new StringBuilder();

        // 遍历 ops 数组
        for (int i = 0; i < ops.size(); i++) {
            JSONObject op = ops.getJSONObject(i);
            // 如果 insert 是字符串，添加到结果中
            if (op.containsKey("insert") && op.get("insert") instanceof String) {
                text.append(op.getString("insert"));
            }
        }

        return text.toString().trim();
    }

}
