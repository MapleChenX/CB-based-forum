package com.example.common;

public class DataSourceContextHolder {

    public static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void set(String dataSource) {
        contextHolder.set(dataSource);
    }

    public static String get() {
        return contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }

}
