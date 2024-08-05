package com.candygoose.translationservice.service;

import com.candygoose.translationservice.exception.InvalidLanguageException;
import com.candygoose.translationservice.exception.TranslationServiceException;
import com.candygoose.translationservice.model.TranslationRecord;
import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.repository.TranslationRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.*;

@Service
public class YandexTranslationService implements TranslationService {
    private final RestTemplate restTemplate;
    private final TranslationRepository translationRepository;
    private final ExecutorService executorService;

    @Value("${yandex.api.url}")
    private String yandexApiUrl;

    @Value("${yandex.api.key}")
    private String yandexApiKey;

    private final Semaphore rateLimiter;

    @Autowired
    public YandexTranslationService(RestTemplate restTemplate,
                                    TranslationRepository translationRepository,
                                    @Value("${translation.rate.limit}") int rateLimit) {
        this.restTemplate = restTemplate;
        this.translationRepository = translationRepository;
        this.executorService = Executors.newFixedThreadPool(10);
        this.rateLimiter = new Semaphore(rateLimit);
    }


    @Override
    public TranslationResponse translate(TranslationRequest request, String ipAddress) {
        List<String> words = List.of(request.getText().split("\\s+"));

        List<CompletableFuture<String>> futures = words.stream()
                .map(word -> CompletableFuture.supplyAsync(() -> {
                    try {
                        rateLimiter.acquire();
                        return translateWord(word, request.getSourceLanguage(), request.getTargetLanguage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new TranslationServiceException("Перевод был прерван");
                    } finally {
                        rateLimiter.release();
                    }
                }, executorService))
                .toList();

        StringBuilder translatedText = new StringBuilder();
        for (CompletableFuture<String> future : futures) {
            try {
                translatedText.append(future.get()).append(" ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InvalidLanguageException) {
                    throw (InvalidLanguageException) cause;
                } else if (cause instanceof TranslationServiceException) {
                    throw (TranslationServiceException) cause;
                } else {
                    throw new TranslationServiceException("Ошибка выполнения задачи перевода");
                }
            }
        }

        TranslationResponse response = new TranslationResponse(translatedText.toString().trim());

        TranslationRecord record = new TranslationRecord(ipAddress, request.getText(), response.getResponseText());
        translationRepository.save(record);

        return response;
    }

    private String translateWord(String word, String sourceLanguage, String targetLanguage) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Api-Key " + yandexApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("{\"sourceLanguageCode\": \"%s\", \"targetLanguageCode\": \"%s\", \"texts\": [\"%s\"]}",
                sourceLanguage, targetLanguage, word);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                yandexApiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (responseEntity.getBody() == null) {
            throw new TranslationServiceException("Ошибка доступа к ресурсу перевода");
        }

        JsonObject rootNode = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
        JsonObject firstTranslation = rootNode.getAsJsonArray("translations").get(0).getAsJsonObject();
        String translatedWord = firstTranslation.get("text").getAsString();
        return translatedWord;
    }
}
