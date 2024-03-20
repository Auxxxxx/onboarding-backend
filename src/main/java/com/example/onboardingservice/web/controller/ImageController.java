package com.example.onboardingservice.web.controller;

import com.example.onboardingservice.exception.DownloadingImagesException;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.service.ImageService;
import com.example.onboardingservice.web.httpData.image.ImageGetMediaAssetsResponse;
import com.example.onboardingservice.web.httpData.image.ImageGetPaidAdvertisingReportsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping(path = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Tag(name = "Image", description = "Endpoints for loading and downloading images")
public class ImageController {
    private final ImageService imageService;

    @Secured("CLIENT")
    @Operation(summary = "Save media assets", description = "Load media assets images into the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data. Accessible only for clients"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Arguments are not base64 encoded images")
    })
    @PutMapping("/media-assets/{clientEmail}")
    public ResponseEntity<Void> putMediaAssets(
            @Parameter(description = """
                    And email of the client who had uploaded the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail,
            @RequestParam("files") MultipartFile[] files) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (clientEmail == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!user.getEmail().equals(clientEmail)) {
                log.error("saving_media_assets: " + clientEmail + " by: " + user.getEmail());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            log.info("saving_media_assets: " + clientEmail);
            imageService.uploadMediaAssets(
                    files,
                    clientEmail);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    @Secured("MANAGER")
    @Operation(summary = "Get media assets", description = "Get media assets for this client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "400", description = "Bad Request. No client specified")
    })
    @GetMapping("/media-assets/{clientEmail}")
    public ResponseEntity<ImageGetMediaAssetsResponse> getMediaAssets(
            @Parameter(description = """
                    And email of the client who had uploaded the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("fetching_media_assets: " + clientEmail);
        var imageUrls = imageService.getMediaAssets(clientEmail);
        log.info(String.join(",", imageUrls));
        var response = ImageGetMediaAssetsResponse.builder()
                .imageUrls(imageUrls)
                .build();
        return ResponseEntity.ok(response);
    }

    @Secured("MANAGER")
    @Operation(summary = "Get media assets zipped", description = "Get zip archive with media assets for this client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "400", description = "Bad Request. No client specified"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. Failed to download objects")
    })
    @GetMapping(value = "/media-assets/zipped/{clientEmail}", produces="application/zip")
    public ResponseEntity<byte[]> getMediaAssetsZipped(
            @Parameter(description = """
                    Email of the client who had uploaded the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("fetching_media_assets_zipped: " + clientEmail);
        try {
            var mediaAssetsZipped = imageService.getMediaAssetsZipped(clientEmail);
            return ResponseEntity.ok(mediaAssetsZipped);
        } catch (DownloadingImagesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Save paid advertising reports", description = "Load paid advertising reports images into the storage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Arguments are not media files")
    })
    @PutMapping("/paid-advertising-reports/{clientEmail}")
    public ResponseEntity<Void> putPaidAdvertisingReports(
            @Parameter(description = """
                    Email of the client who had uploaded the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail,
            @RequestParam("files") MultipartFile[] files) {
        try {
            if (clientEmail == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            log.info("saving_paid_advertising_reports: " + clientEmail);
            imageService.uploadPaidAdvertisingReports(
                    files,
                    clientEmail);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Get paid advertising reports", description = "Get paid advertising reports for this client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data. Accessible only for clients"),
            @ApiResponse(responseCode = "400", description = "Bad Request. No client specified")
    })
    @GetMapping("/paid-advertising-reports/{clientEmail}")
    public ResponseEntity<ImageGetPaidAdvertisingReportsResponse> getPaidAdvertisingReports(
            @Parameter(description = """
                    And email of the client who should see the images
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("fetching_paid_advertising_reports: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("fetching_paid_advertising_reports: " + clientEmail);
        var imageUrls = imageService.getPaidAdvertisingReports(clientEmail);
        log.info(String.join(",", imageUrls));
        var response = ImageGetPaidAdvertisingReportsResponse.builder()
                .imageUrls(imageUrls)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get paid advertising reports zipped", description = "Get zip archive with paid advertising reports for this client.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to access another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. No client specified"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. Failed to download objects")
    })
    @GetMapping(value = "/paid-advertising-reports/zipped/{clientEmail}", produces="application/zip")
    public ResponseEntity<byte[]> getPaidAdvertisingReportsZipped(
            @Parameter(description = """
                    Email of the client who will receive the reports
                    """, required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("fetching_paid_advertising_reports_zipped: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("fetching_paid_advertising_reports_zipped: " + clientEmail);
        try {
            var paidAdvertisingReportsZipped = imageService.getPaidAdvertisingReportsZipped(clientEmail);
            return ResponseEntity.ok(paidAdvertisingReportsZipped);
        } catch (DownloadingImagesException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}