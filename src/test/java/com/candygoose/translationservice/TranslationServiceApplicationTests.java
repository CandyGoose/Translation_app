package com.candygoose.translationservice;

import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.service.TranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TranslationServiceApplicationTests {

    @Autowired
    private TranslationService translationService;

    @Test
    void contextLoads() {
        assertNotNull(translationService);
    }

    @Test
    void testBasicTranslation() {
        TranslationRequest request = new TranslationRequest("en", "ru", "World");
        TranslationResponse response = translationService.translate(request, "127.0.0.1");

        assertNotNull(response, "Ответ не должен быть null");
        assertEquals("Мир", response.getResponseText(), "Перевод должен соответствовать ожидаемому результату");
    }
}