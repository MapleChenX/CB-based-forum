package com.example.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 用于DTO快速转换VO实现，只需将DTO类继承此类即可使用
 */
public interface BaseData {
    /**
     * 创建指定的VO类并将当前DTO对象中的所有成员变量值直接复制到VO对象中
     * @param clazz 指定VO类型
     * @param consumer 返回VO对象之前可以使用Lambda进行额外处理
     * @return 指定VO对象
     * @param <V> 指定VO类型
     */
    // 默认<V> V asViewObject(Class<V> clazz, Consumer<V> consumer)方法，用于将当前对象转换为指定类型的视图对象，并使用consumer方法对视图对象进行操作
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer) {
        // 将当前对象转换为指定类型的视图对象
        V v = this.asViewObject(clazz);
        // 使用consumer方法对视图对象进行操作
        consumer.accept(v);
        // 返回视图对象
        return v;
    }

    /**
     * 创建指定的VO类并将当前DTO对象中的所有成员变量值直接复制到VO对象中
     * @param clazz 指定VO类型
     * @return 指定VO对象
     * @param <V> 指定VO类型
     */
    default <V> V asViewObject(Class<V> clazz) {
        try {
            // 获取类中声明的属性
            Field[] fields = clazz.getDeclaredFields();
            // 获取类的构造函数
            Constructor<V> constructor = clazz.getConstructor();
            // 实例化对象
            V v = constructor.newInstance();
            // 遍历属性，将属性值转换为对象
            Arrays.asList(fields).forEach(field -> convert(field, v));
            return v;
        } catch (ReflectiveOperationException exception) {
            Logger logger = LoggerFactory.getLogger(BaseData.class);
            logger.error("在VO与DTO转换时出现了一些错误", exception);
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * 内部使用，快速将当前类中目标对象字段同名字段的值复制到目标对象字段上
     * @param field 目标对象字段
     * @param target 目标对象
     */
    private void convert(Field field, Object target){
        try {
            //获取当前类中的field属性
            Field source = this.getClass().getDeclaredField(field.getName());
            //设置当前类的属性可访问
            field.setAccessible(true);
            //设置源类的属性可访问
            source.setAccessible(true);
            //将源类的属性值赋值给当前类的属性
            field.set(target, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {}
    }
}
