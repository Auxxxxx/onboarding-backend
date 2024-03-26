package com.example.onboardingservice.model.dto;

import com.example.onboardingservice.model.Report;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class ReportWithImagesDto {
    private Long reportId;
    private Report reportData;
    private List<String> imageUrls;
    private Long sizeKb;
}
