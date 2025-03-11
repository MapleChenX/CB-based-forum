package com.example.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.example.mapper.mysql", sqlSessionFactoryRef = "mysqlSqlSessionFactory")
public class MySQLMapperConfig {
}
