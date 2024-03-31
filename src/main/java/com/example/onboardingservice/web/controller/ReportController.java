package com.example.onboardingservice.web.controller;

import com.example.onboardingservice.exception.*;
import com.example.onboardingservice.model.Role;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.service.ImageService;
import com.example.onboardingservice.service.NoteService;
import com.example.onboardingservice.service.ReportService;
import com.example.onboardingservice.web.httpData.note.*;
import com.example.onboardingservice.web.httpData.report.ReportDeleteResponse;
import com.example.onboardingservice.web.httpData.report.ReportGetByIdResponse;
import com.example.onboardingservice.web.httpData.report.ReportGetResponse;
import com.example.onboardingservice.web.util.RequestData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(path = "/report", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Tag(name = "Report", description = "Endpoints for CRUD operations on reports")
public class ReportController {
    private final ReportService reportService;
    private final ImageService imageService;


    @Operation(summary = "Get reports", description = "Lists all reports for the client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null")
    })
    @GetMapping("/{clientEmail}")
    public ResponseEntity<ReportGetResponse> getReports(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(clientEmail)) {
            log.error("returning_reports: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_reports: " + clientEmail);
        var reports = reportService.listReportsWithImages(clientEmail);
        var response = ReportGetResponse.builder()
                .reports(reports)
                .build();
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Get report by id", description = "Returns a report by email and id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null")
    })
    @GetMapping("/{clientEmail}/{reportId}")
    public ResponseEntity<ReportGetByIdResponse> getReportById(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail,
            @RequestBody(description = "Report id", required = true)
            @PathVariable("reportId") Long reportId) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(clientEmail)) {
            log.error("returning_report_by_id: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_report_by_id: " + clientEmail);
        try {
            var report = reportService.findReportById(clientEmail, reportId);
            var response = ReportGetByIdResponse.builder()
                    .report(report)
                    .build();
            return ResponseEntity.ok(response);
        } catch (ReportNotFoundException e) {
            log.error("report_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Save paid advertising report", description = "Upload paid advertising reports images into the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Arguments are not media files")
    })
    @PutMapping("/{clientEmail}")
    public ResponseEntity<Void> putPaidAdvertisingReports(
            @Parameter(description = """
                    Email of the client who receives the messages
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail,
            @RequestParam("reportName") String reportName,
            @RequestParam("files") MultipartFile[] files) {
        if (clientEmail == null ||
                clientEmail.isBlank() ||
                reportName == null ||
                reportName.isBlank() ||
                files.length == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("saving_paid_advertising_report: " + clientEmail);
        try {
            reportService.save(
                    clientEmail,
                    reportName,
                    files);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UserNotFoundException e) {
            log.error("user_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get a report zipped", description = "Get zip archive with report for this client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. No client specified"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. Failed to download objects")
    })
    @GetMapping(value = "/zipped/{clientEmail}/{reportId}", produces = "application/zip")
    public ResponseEntity<byte[]> getReportZipped(
            @Parameter(description = """
                    Email of the client who had uploaded the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail,
            @PathVariable("reportId") Long reportId) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(clientEmail)) {
            log.error("fetching_report_zipped: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("fetching_report_zipped: " + clientEmail);
        try {
            var reportZipped = imageService.getPaidAdvertisingReportZipped(clientEmail, reportId);
            return ResponseEntity.ok(reportZipped);
        } catch (DownloadingImagesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ReportDeleteResponse> deleteReport(
            @RequestBody(description = "Id of the report to delete", required = true)
            @PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("deleting_report: " + id);
        try {
            var reports = reportService.deleteReportById(id);
            var response = ReportDeleteResponse.builder()
                    .reports(reports)
                    .build();
            return ResponseEntity.ok(response);
        } catch (ReportNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}