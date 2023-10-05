package org.example;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUpdater {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydatabase";
    private static final String DB_USER = "username";
    private static final String DB_PASSWORD = "password";
    private static final String UPDATES_DIRECTORY = "path/to/updates";

    public static void main(String[] args) {
        // Подключение к базе данных
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Получение списка файлов обновлений
            List<File> updateFiles = getUpdateFiles();

            // Применение обновлений в определенном порядке
            for (File file : updateFiles) {
                applyUpdate(conn, file);
            }

            System.out.println("Database updates applied successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    private static List<File> getUpdateFiles() {
        List<File> updateFiles = new ArrayList<>();

        File directory = new File(UPDATES_DIRECTORY);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        updateFiles.add(file);
                    }
                }
            }
        } else {
            System.err.println("Updates directory does not exist.");
        }

        return updateFiles;
    }

    private static void applyUpdate(Connection conn, File file) {
        try (Statement stmt = conn.createStatement()) {
            // Чтение SQL-скрипта обновления из файла
            StringBuilder sqlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlBuilder.append(line);
                    sqlBuilder.append("\n");
                }
            } catch (IOException e) {
                System.err.println("Failed to read update file: " + file.getName());
                return;
            }

            String sql = sqlBuilder.toString();

            // Выполнение SQL-запроса обновления
            try {
                stmt.executeUpdate(sql);
                System.out.println("Applied update from file: " + file.getName());
            } catch (SQLException e) {
                System.err.println("Failed to apply update from file: " + file.getName());
                System.err.println("Error message: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Failed to create statement: " + e.getMessage());
        }
    }
}