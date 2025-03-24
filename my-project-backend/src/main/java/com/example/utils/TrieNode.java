package com.example.utils;


import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEnd = false; // 是否是敏感词结尾
}
