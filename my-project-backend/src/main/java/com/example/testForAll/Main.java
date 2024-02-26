package com.example.testForAll;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.example.testForAll.wheel.BeanUtilss;
import com.example.testForAll.wheel.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog();
        dog.setAge(1);
        dog.setName("dog");
        dog.setHomeAddress("here");
        dog.setSex("male");

        Duck duck = new Duck();
        BeanUtilss.copyProperties(dog, duck);

        // 输出duck的全部属性值
        System.out.println(duck);

        // jackson
        // 创建ObjectMapper对象
        ObjectMapper mapper = new ObjectMapper();
        try {
            // 将对象转成json字符串
            String json = mapper.writeValueAsString(duck);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // 使用fastjson输出duck的全部属性值
        String json = JSON.toJSONString(duck, JSONWriter.Feature.WriteNulls);
        System.out.println("alibaba     "+json);

        // 自定义对象转JSON
        String json2 = JsonUtils.toJsonString(duck, true);
        System.out.println("自定义对象转JSON     "+json2);

    }
}
