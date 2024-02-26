package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@Data
@TableName("db_account_privacy")
public class AccountPrivacy implements BaseData {
    @TableId(type = IdType.AUTO)
    final Integer id;
    boolean phone = true;
    boolean email = true;
    boolean wx = true;
    boolean qq = true;
    boolean gender = true;

    // 查看当前对象中值为 false 的字段名
    public String[] hiddenFields(){
        // 为什么这里要用 List 而不是 String[]？
        // 因为 String[] 的长度是固定的，而 List 的长度是可变的。我们并不知道要忽略的字段有多少个。
        List<String> strings = new LinkedList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                // !field.getBoolean(this) 检查当前字段的值是否为 false。这里的 this 指的是当前的 AccountPrivacy 对象。
                 if(field.getType().equals(boolean.class) && !field.getBoolean(this))
                     strings.add(field.getName());
            } catch (Exception ignored) {}
        }
        return strings.toArray(String[]::new);
    }
}
