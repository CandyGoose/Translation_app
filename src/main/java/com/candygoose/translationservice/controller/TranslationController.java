package com.candygoose.translationservice.controller;

import com.candygoose.translationservice.exception.InvalidLanguageException;
import com.candygoose.translationservice.exception.TranslationServiceException;
import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> translate(
            @RequestParam String sourceLanguage,
            @RequestParam String targetLanguage,
            @RequestParam String text,
            HttpServletRequest httpRequest) {

        if (sourceLanguage.isBlank() || targetLanguage.isBlank() || text.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TranslationResponse("Исходный язык, целевой язык и текст не должны быть пустыми"));
        }

        TranslationRequest translationRequest = new TranslationRequest(sourceLanguage, targetLanguage, text);
        String ipAddress = httpRequest.getRemoteAddr();

        try {
            TranslationResponse response = translationService.translate(translationRequest, ipAddress);
            return ResponseEntity.ok(response);
        } catch (InvalidLanguageException | TranslationServiceException e) {
            HttpStatus status = e instanceof InvalidLanguageException ? HttpStatus.BAD_REQUEST : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(new TranslationResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TranslationResponse("Внутренняя ошибка сервера"));
        }
    }
}
