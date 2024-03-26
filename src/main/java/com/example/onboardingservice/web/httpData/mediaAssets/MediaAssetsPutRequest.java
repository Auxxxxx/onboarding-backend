package com.example.onboardingservice.web.httpData.mediaAssets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaAssetsPutRequest {
    private Map<String, String> imagesBase64;
    private String clientEmail;
}
