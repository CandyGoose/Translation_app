package com.candygoose.translationservice.controller;

import com.candygoose.translationservice.exception.InvalidLanguageException;
import com.candygoose.translationservice.exception.TranslationServiceException;
import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private static final Logger logger = LoggerFactory.getLogger(TranslationController.class);

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
        String safeSourceLanguage = StringEscapeUtils.escapeHtml4(sourceLanguage);
        String safeTargetLanguage = StringEscapeUtils.escapeHtml4(targetLanguage);
        String safeText = StringEscapeUtils.escapeHtml4(text);

        if (safeSourceLanguage.isBlank() || safeTargetLanguage.isBlank() || safeText.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TranslationResponse("Исходный язык, целевой язык и текст не должны быть пустыми"));
        }

        TranslationRequest translationRequest = new TranslationRequest(safeSourceLanguage, safeTargetLanguage, safeText);
        String ipAddress = httpRequest.getRemoteAddr();

        logger.info("Получен запрос на перевод с IP: {}, Исходный язык: {}, Целевой язык: {}, Текст: {}",
                ipAddress, safeSourceLanguage, safeTargetLanguage, safeText);
        try {
            TranslationResponse response = translationService.translate(translationRequest, ipAddress);
            logger.info("Перевод успешен для запроса с IP: {}, Переведённый текст: {}", ipAddress, response.getResponseText());
            return ResponseEntity.ok(response);
        } catch (InvalidLanguageException | TranslationServiceException e) {
            logger.error("Ошибка для запроса с IP: {}: {}", ipAddress, e.getMessage());
            HttpStatus status = e instanceof InvalidLanguageException ? HttpStatus.BAD_REQUEST : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(status).body(new TranslationResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Ошибка для запроса с IP: {}: {}", ipAddress, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new TranslationResponse("Внутренняя ошибка сервера"));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<TranslationResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.error("Отсутствует обязательный параметр запроса для IP: {}: {}", request.getRemoteAddr(), ex.getParameterName());
        String errorMessage = "Отсутствует обязательный параметр запроса: " + ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new TranslationResponse(errorMessage));
    }
}
