package com.candygoose.translationservice.service;

import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;
import org.springframework.stereotype.Service;

@Service
public class BasicTranslationService implements TranslationService {

    @Override
    public TranslationResponse translate(TranslationRequest request) {
        return new TranslationResponse(request.getText());
    }
}
