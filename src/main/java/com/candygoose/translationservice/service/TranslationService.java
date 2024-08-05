package com.candygoose.translationservice.service;

import com.candygoose.translationservice.model.TranslationRequest;
import com.candygoose.translationservice.model.TranslationResponse;

public interface TranslationService {
    TranslationResponse translate(TranslationRequest request);
}
