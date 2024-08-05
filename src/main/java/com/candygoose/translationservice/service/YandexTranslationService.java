package com.candygoose.translationservice.service;

import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class YandexTranslationService implements TranslationService {

    @Value("${yandex.api.url}")
    private String yandexApiUrl;

    @Value("${yandex.api.key}")
    private String yandexApiKey;

    private final RestTemplate restTemplate;

    public YandexTranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public TranslationResponse translate(TranslationRequest request, String ipAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Api-Key " + yandexApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("{\"sourceLanguageCode\": \"%s\", \"targetLanguageCode\": \"%s\", \"texts\": [\"%s\"]}",
                request.getSourceLanguage(), request.getTargetLanguage(), request.getText());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                yandexApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (responseEntity.getBody() == null) {
            throw new RuntimeException("Получен пустой ответ");
        }

        String translatedText = extractTranslatedText(responseEntity.getBody());
        return new TranslationResponse(translatedText);
    }

    private String extractTranslatedText(String responseBody) {
        try {
            JsonObject rootNode = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject translationNode = rootNode.getAsJsonArray("translations").get(0).getAsJsonObject();
            return translationNode.get("text").getAsString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения задачи перевода", e);
        }
    }
}