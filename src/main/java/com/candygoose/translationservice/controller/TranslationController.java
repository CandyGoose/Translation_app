package com.candygoose.translationservice.controller;

import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import com.candygoose.translationservice.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public ResponseEntity<TranslationResponse> translate(@RequestParam String sourceLanguage,
                                                         @RequestParam String targetLanguage,
                                                         @RequestParam String text) {
        TranslationRequest translationRequest = new TranslationRequest(sourceLanguage, targetLanguage, text);
        TranslationResponse response = translationService.translate(translationRequest);
        return ResponseEntity.ok(response);
    }
}
