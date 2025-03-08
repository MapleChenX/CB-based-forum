package com.example.testForAll.wheel;// MySQLToFileExporter.java
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FakePost {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spider";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "111111";
    private static final String OUTPUT_FILE = "x://urls.txt";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT url FROM preview");
             BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {

            while (resultSet.next()) {
                String url = resultSet.getString("url");
                writer.write(url);
                writer.newLine();
            }

            System.out.println("Data exported successfully to " + OUTPUT_FILE);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}