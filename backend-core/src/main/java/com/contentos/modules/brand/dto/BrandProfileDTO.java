package com.contentos.modules.brand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfileDTO {
    private UUID id;
    private UUID workspaceId;
    private String brandName;
    private String industry;
    private String targetAudience;
    private String toneOfVoice;
    private String writingStyle;
    private String primaryColor;
    private String secondaryColor;
    private String fontPrimary;
    private String fontSecondary;
    private String logoUrl;
    private String brandGuidelines;
    private List<String> wordsToAvoid;
    private List<String> preferredPhrases;
    private String ctaPreferences;
}
