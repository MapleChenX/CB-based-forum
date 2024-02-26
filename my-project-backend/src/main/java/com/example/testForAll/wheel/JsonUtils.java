package com.example.testForAll.wheel;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    public static String toJsonString(Object obj, boolean writeNulls) {
        StringBuilder json = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                // 获取字段值
                Object value = field.get(obj);
                if (value != null || writeNulls) {
                    // 添加字段名
                    json.append("\"").append(field.getName()).append("\":");
                    // 字段是Number类型就不用加引号
                    if (value instanceof Number) {
                        json.append(value);
                    } else {
                        json.append("\"").append(value).append("\"");
                    }
                    json.append(",");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }
}
