package com.example.onboardingservice.web.httpData.report;

import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.Report;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDeleteResponse {
    private List<Report> reports;
}
