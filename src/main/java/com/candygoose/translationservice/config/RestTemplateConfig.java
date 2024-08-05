package com.candygoose.translationservice.config;

import com.candygoose.translationservice.exception.InvalidLanguageException;
import com.candygoose.translationservice.exception.TranslationServiceException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        return restTemplate;
    }

    static class CustomResponseErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            int statusCode = response.getStatusCode().value();
            String responseBody = new String(response.getBody().readAllBytes());

            JsonObject errorNode;
            String errorMessage;

            try {
                errorNode = JsonParser.parseString(responseBody).getAsJsonObject();
                errorMessage = errorNode.has("message") ? errorNode.get("message").getAsString() : "Неизвестная ошибка";
            } catch (Exception e) {
                throw new TranslationServiceException("Ошибка: " + e.getMessage());
            }

            if (statusCode >= 400 && statusCode < 500) {
                if (errorMessage.contains("unsupported source_language_code") || errorMessage.contains("unsupported target_language_code")) {
                    String unsupportedLanguage = errorMessage.contains("source_language_code") ? "Исходный" : "Целевой";
                    throw new InvalidLanguageException(unsupportedLanguage + " язык не поддерживается");
                } else {
                    throw new InvalidLanguageException("Ошибка клиента: " + errorMessage);
                }
            } else if (statusCode >= 500 && statusCode < 600) {
                throw new TranslationServiceException("Ошибка сервера: " + errorMessage);
            }
        }
    }
}
