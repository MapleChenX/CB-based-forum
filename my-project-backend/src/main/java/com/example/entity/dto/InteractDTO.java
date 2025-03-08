package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
public class InteractDTO {

    Integer tid;
    Integer uid;
    Date time;

}
