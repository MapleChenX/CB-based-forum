//package com.example.config;
//
//import com.example.common.DataSourceContextHolder;
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//
//
///**
// * 动态数据源
// */
//public class RoutingDataSource extends AbstractRoutingDataSource {
//    @Override
//    protected Object determineCurrentLookupKey() {
//        return DataSourceContextHolder.get();
//    }
//
//}
