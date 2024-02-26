package com.example.testForAll;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.function.Consumer;
@Slf4j
public class ChangeProperties{
    public void copyProperties(Object source, Object target, Consumer<Object> consumer) {
        try{
        BeanUtils.copyProperties(source, target);
        consumer.accept(target);
        }catch (Exception e){
            System.out.println("copyProperties error");
            log.error(e.toString());
        }
    }
}
