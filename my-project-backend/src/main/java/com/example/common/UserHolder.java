package com.example.common;

public class UserHolder {
    private static final ThreadLocal<Integer> userHolder = new ThreadLocal<>();

    public static void set(Integer userId) {
        userHolder.set(userId);
    }

    public static Integer get() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }
}
