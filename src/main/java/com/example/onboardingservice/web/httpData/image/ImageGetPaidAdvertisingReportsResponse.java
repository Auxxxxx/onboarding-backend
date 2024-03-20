package com.example.onboardingservice.web.httpData.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageGetPaidAdvertisingReportsResponse {
    private List<String> imageUrls;
}
