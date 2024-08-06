package com.candygoose.translationservice.repository;

import com.candygoose.translationservice.model.TranslationRecord;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class TranslationRepository {

    private static final Logger logger = LoggerFactory.getLogger(TranslationRepository.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.table.name}")
    private String tableName;

    @PostConstruct
    private void initializeDatabase() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id SERIAL PRIMARY KEY, " +
                "ip_address VARCHAR(45), " +
                "original_text TEXT, " +
                "translated_text TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement createTableStatement = connection.prepareStatement(createTableQuery)) {
            createTableStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Ошибка при создании таблицы: ", e);
        }
    }

    public void save(TranslationRecord record) {
        String query = "INSERT INTO " + tableName + " (ip_address, original_text, translated_text) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, record.getIpAddress());
            preparedStatement.setString(2, sanitizeText(record.getOriginalText()));
            preparedStatement.setString(3, sanitizeText(record.getTranslatedText()));

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Запись перевода успешна сохранена: {}", record);
            } else {
                logger.warn("Ни одной строки не было затронуто при сохранении записи перевода: {}", record);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при сохранении записи перевода: {}", record, e);
        }
    }

    private String sanitizeText(String text) {
        return text.replaceAll("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "").trim();
    }
}
