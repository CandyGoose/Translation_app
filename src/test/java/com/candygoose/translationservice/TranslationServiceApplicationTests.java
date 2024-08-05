package com.candygoose.translationservice;

import com.candygoose.translationservice.exception.InvalidLanguageException;
import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.service.TranslationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testTranslationWithInvalidLanguage() {
        TranslationRequest request = new TranslationRequest("en", "xx", "Hello");

        InvalidLanguageException exception = assertThrows(InvalidLanguageException.class, () -> translationService.translate(request, "127.0.0.1"));

        assertEquals("Целевой язык не поддерживается", exception.getMessage(), "Перевод должен соответствовать ожидаемому результату");
    }

    @Test
    void testTranslationWithSpecialCharacters() {
        TranslationRequest request = new TranslationRequest("en", "ru", "World! 123 #@$%");

        TranslationResponse response = translationService.translate(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("Мир! 123 #@$%", response.getResponseText(), "В переводе должны быть сохранены специальные символы и цифры");
    }

    @Test
    void testTranslationWithLongText() {
        String longText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(20);
        TranslationRequest request = new TranslationRequest("la", "ru", longText);

        TranslationResponse response = translationService.translate(request, "127.0.0.1");

        assertNotNull(response, "Ответ не должен быть null");
        assertFalse(response.getResponseText().isEmpty(), "Перевод должен возвращать непустой результат для длинного текста");
    }

    @Test
    void testTranslationWithVeryLongTextFromFileUsingClassLoader() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("long_text.txt")).getFile());

        String longText = new String(Files.readAllBytes(file.toPath()));

        TranslationRequest request = new TranslationRequest("ru", "ar", longText);
        TranslationResponse response = translationService.translate(request, "127.0.0.1");

        assertNotNull(response, "Ответ не должен быть null");
        assertFalse(response.getResponseText().isEmpty(), "Перевод должен возвращать непустой результат для длинного текста");
    }

    @Test
    void testTranslationWithEdgeCaseLanguageCodes() {
        TranslationRequest request = new TranslationRequest("en", "ja", "Hello!");
        TranslationResponse response = translationService.translate(request, "127.0.0.3");

        assertNotNull(response, "Ответ не должен быть null");
        assertEquals("こんにちは!", response.getResponseText(), "Перевод должен соответствовать ожидаемому результату");

        request = new TranslationRequest("en", "fr", "Hello");
        response = translationService.translate(request, "127.0.0.1");

        assertNotNull(response, "Ответ не должен быть null");
        assertEquals("Bonjour", response.getResponseText(), "Перевод должен соответствовать ожидаемому результату");
    }
}
