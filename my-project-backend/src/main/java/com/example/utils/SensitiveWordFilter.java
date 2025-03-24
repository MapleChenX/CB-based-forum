package com.example.utils;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class SensitiveWordFilter {
    private final TrieNode root = new TrieNode();

    // Bean 初始化时加载敏感词
    @PostConstruct
    public void init() {
        loadSensitiveWords("/shitDict.txt"); // 读取 `resources` 目录下的敏感词文件
    }

    // 加载敏感词文件
    private void loadSensitiveWords(String filePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addWord(line.trim());
            }
        } catch (Exception e) {
            throw new RuntimeException("敏感词文件加载失败", e);
        }
    }

    // 添加敏感词到 Trie 树
    private void addWord(String word) {
        if (word.isEmpty()) return;
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    // 过滤敏感词
    public String filter(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder result = new StringBuilder(text);
        TrieNode node;
        int i = 0, j;

        while (i < text.length()) {
            node = root;
            j = i;
            while (j < text.length() && node.children.containsKey(text.charAt(j))) {
                node = node.children.get(text.charAt(j));
                if (node.isEnd) {
                    for (int k = i; k <= j; k++) {
                        result.setCharAt(k, '*'); // 替换敏感词
                    }
                }
                j++;
            }
            i++;
        }
        return result.toString();
    }

    // Trie 树节点类
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }
}
