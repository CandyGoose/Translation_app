package com.candygoose.translationservice.repository;

import com.candygoose.translationservice.model.TranslationRecord;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class TranslationRepository {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.table}")
    private String table;

    @PostConstruct
    private void initializeDatabase() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "id SERIAL PRIMARY KEY, " +
                "ip_address VARCHAR(45), " +
                "original_text TEXT, " +
                "translated_text TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement createTableStatement = connection.prepareStatement(createTableQuery)) {
            createTableStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void save(TranslationRecord record) {
        String query = "INSERT INTO " + table + " (ip_address, original_text, translated_text) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, record.getIpAddress());
            preparedStatement.setString(2, record.getOriginalText());
            preparedStatement.setString(3, record.getTranslatedText());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
