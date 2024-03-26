package com.example.onboardingservice.web.httpData.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportPutRequest {
    @Schema(example = "report1")
    private String name;
    @Schema(example = "Useful info")
    private String header;
}