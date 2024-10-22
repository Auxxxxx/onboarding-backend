package com.example.onboardingservice.service;

import com.example.onboardingservice.exception.JsonTooLongException;
import com.example.onboardingservice.exception.ReportNotFoundException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongListSize;
import com.example.onboardingservice.model.Client;
import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.NoteType;
import com.example.onboardingservice.model.Report;
import com.example.onboardingservice.model.dto.ReportWithImagesDto;
import com.example.onboardingservice.repository.ReportRepository;
import com.example.onboardingservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ReportService {
    private final UserService userService;
    private final ReportRepository reportRepository;
    private final ImageService imageService;
    private static final int BYTES_PER_KILOBYTE = 1000;

    private List<Report> listReports(String email) {
        List<Report> reports = reportRepository.findByRecipient(email);
        return reports.stream()
                .filter(report -> report.getRemovedAt() == null)
                .sorted((r1, r2) -> r2.getDate().compareTo(r1.getDate()))
                .toList();
    }

    public List<ReportWithImagesDto> listReportsWithImages(String email) {
        return listReports(email).stream()
                .map(report -> {
                    List<String> imageUrls = imageService.getPaidAdvertisingReport(email, report.getId());
                    long sizeBytes = imageService.getPaidAdvertisingReportSize(email, report.getId());
                    return ReportWithImagesDto.builder()
                            .reportId(report.getId())
                            .reportData(report)
                            .imageUrls(imageUrls)
                            .sizeKb(sizeBytes / BYTES_PER_KILOBYTE)
                            .build();
                })
                .toList();
    }

    public ReportWithImagesDto findReportById(String email, Long reportId) throws ReportNotFoundException {
        Report reportData = reportRepository.findByRecipientAndId(email, reportId).orElseThrow(ReportNotFoundException::new);
        return ReportWithImagesDto.builder()
                .reportData(reportData)
                .reportId(reportId)
                .imageUrls(imageService.getPaidAdvertisingReport(email, reportId))
                .sizeKb(imageService.getPaidAdvertisingReportSize(email, reportId) / BYTES_PER_KILOBYTE)
                .build();
    }

    @Transactional
    public void save(String clientEmail, String name, MultipartFile[] files) throws UserNotFoundException, IOException {
        Client recipient = (Client) userService.findByEmail(clientEmail);
        Report report = Report.builder()
                .recipient(recipient)
                .date(LocalDate.now())
                .name(name)
                .build();
        Report saved = reportRepository.save(report);
        Long reportId = saved.getId();
        imageService.uploadPaidAdvertisingReport(files, clientEmail, reportId);
    }

    @Transactional
    public List<Report> deleteReportById(Long id) throws ReportNotFoundException {
        Report report = reportRepository.findById(id).orElseThrow(ReportNotFoundException::new);
        report.setRemovedAt(LocalDate.now());
        reportRepository.save(report);
        return listReports(report.getRecipient().getEmail());
    }
}
