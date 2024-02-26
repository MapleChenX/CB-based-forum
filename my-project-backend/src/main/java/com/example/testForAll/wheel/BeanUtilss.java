package com.example.testForAll.wheel;


import java.lang.reflect.Field;
import java.util.function.Consumer;

public class BeanUtilss {


    public static <V> void copyProperties(Object source, V target, Consumer<V> consumer){
        copyProperties(source, target);
        consumer.accept(target);
    }

    /**
     * 将源对象中的属性值复制到目标对象中
     * 对象->类->字段->字段名和类型匹配->拷贝（目标字段.set(目标对象, 源字段.get(源对象))）
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        // 获取源对象和目标对象的类
        Class<?> sc = source.getClass();
        Class<?> tc = target.getClass();
        // 获取源对象和目标对象的所有字段
        Field[] sf = sc.getDeclaredFields();
        Field[] tf = tc.getDeclaredFields();

        // 字段匹配上了就拷贝过去
        for(Field s : sf){
            s.setAccessible(true);
            for(Field t : tf){
                t.setAccessible(true);
                // 单个字段名和类型都相同的话
                if(s.getName().equals(t.getName()) && s.getType().equals(t.getType())){
                    try {
                        // 目标字段.set(目标对象, 源字段.get(源对象))
                        t.set(target, s.get(source));
                    }catch (IllegalAccessException e){
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
