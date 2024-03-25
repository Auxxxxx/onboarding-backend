package com.example.onboardingservice.service;

import com.example.onboardingservice.model.Report;
import com.example.onboardingservice.model.dto.ReportWithImagesDto;
import com.example.onboardingservice.repository.ReportRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ImageService imageService;
    private static final int BYTES_PER_KILOBYTE = 1000;

    public List<ReportWithImagesDto> listReports(String email) {
        List<Report> reports = reportRepository.findByRecipient(email);
        return reports.stream()
                .map(report -> {
                    List<String> imageUrls = imageService.getPaidAdvertisingReport(email, report.getId());
                    long sizeBytes = imageService.getPaidAdvertisingReportSize(email, report.getId());
                    return ReportWithImagesDto.builder()
                            .report(report)
                            .imageUrls(imageUrls)
                            .sizeKb(sizeBytes / BYTES_PER_KILOBYTE)
                            .build();
                })
                .toList();
    }
}
