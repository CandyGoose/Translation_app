package com.candygoose.translationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRecord {
    private String ipAddress;
    private String originalText;
    private String translatedText;
}
