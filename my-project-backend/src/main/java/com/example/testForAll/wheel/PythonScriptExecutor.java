//package com.example.testForAll.wheel;
//
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//public class PythonScriptExecutor {
//    public static void main(String[] args) {
//        try {
//            // Python 脚本路径
//            String pythonScriptPath = "python C:\\Users\\25056\\Desktop\\Over\\forum\\py_scripts\\scripts\\text2vector.py";  // 修改为实际路径
//
//            // 输入参数：title 和 content
//            String title = "Elasticsearch 向量搜索";
//            String content = "如何使用 Elasticsearch 进行向量搜索？";
//
//            // 构造命令：将参数传递给 Python 脚本
//            ProcessBuilder processBuilder = new ProcessBuilder(pythonScriptPath, title, content);
//
//            // 启动进程并等待执行
//            Process process = processBuilder.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//            String line;
//            StringBuilder output = new StringBuilder();
//
//            // 读取 Python 脚本的输出
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            // 等待 Python 脚本执行完成
//            process.waitFor();
//
//            // 打印输出内容
//            String result = output.toString().trim();
//            System.out.println("Python script output: " + result);
//
//            // 将输出的字符串转为向量（这里是一个简单的示例，你可以根据需要进一步处理）
//            String[] values = result.substring(1, result.length() - 1).split(", ");
//            double[] vector = new double[values.length];
//            for (int i = 0; i < values.length; i++) {
//                vector[i] = Double.parseDouble(values[i]);
//            }
//
//            // 打印解析后的结果（向量）
//            System.out.println("Parsed vector: ");
//            for (double v : vector) {
//                System.out.print(v + " ");
//            }
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}
