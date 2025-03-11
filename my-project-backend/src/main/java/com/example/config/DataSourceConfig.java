package com.example.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * datasource(数据源配置) -> routingDataSource(载入全部数据源) -> sqlSessionFactory(工厂) -> transactionManager(事务管理) -> RoutingDataSource决定键源于Holder
 * 1-创建Holder
 * 2-编写RoutingDataSource继承AbstractRoutingDataSource，重写determineCurrentLookupKey()方法，决定键源于Holder
 * 3-配置类注入所有数据源到RoutingDataSource
 */
@Configuration
public class DataSourceConfig {

    /**
     * mysql datasource
     */
    @Primary
    @Bean(name = "dataSource1")
    @ConfigurationProperties(prefix = "spring.datasource.ds1")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * postgresql datasource
     */
    @Bean(name = "dataSource2")
    @ConfigurationProperties(prefix = "spring.datasource.ds2")
    public DataSource postgresqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * routingDataSource载入全部数据源
     */
    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(@Qualifier("dataSource1") DataSource dataSource1,
                                        @Qualifier("dataSource2") DataSource dataSource2) {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        // 设置数据源 Map（targetDataSources）
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("dataSource1", dataSource1);
        targetDataSources.put("dataSource2", dataSource2);

        routingDataSource.setDefaultTargetDataSource(dataSource1);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet(); // 将targetDataSources resolve了，必须写，相当于这时才真正载入了数据源到正确位置，以供determineCurrentLookupKey调用

        return routingDataSource;
    }

    /**
     * sqlSessionFactory
     */
    @Primary
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("routingDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        return sessionFactory.getObject();
    }

    /**
     * transactionManager
     */
    @Primary
    @Bean
    public DataSourceTransactionManager transactionManager(@Qualifier("routingDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}

