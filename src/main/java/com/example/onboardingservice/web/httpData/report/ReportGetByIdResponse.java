package com.example.onboardingservice.web.httpData.report;

import com.example.onboardingservice.model.dto.ReportWithImagesDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportGetByIdResponse {
    private ReportWithImagesDto report;
}
