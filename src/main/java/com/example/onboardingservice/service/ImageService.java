package com.example.onboardingservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.onboardingservice.exception.DownloadingImagesException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    @Value("${storage.base-url}")
    private String baseUrl;
    @Value("${storage.root}")
    private String bucket;
    private final AmazonS3 s3;

    @Transactional
    public void uploadMediaAssets(MultipartFile[] files, String clientEmail) throws IOException {
        saveImages(files, "media-assets", clientEmail);
    }

    @Transactional
    public void uploadPaidAdvertisingReport(MultipartFile[] files, String clientEmail, Long reportId) throws IOException {
        saveImages(files,"paid-advertising-reports", clientEmail, reportId.toString());
    }


    private void saveImages(MultipartFile[] files, String... dirPath) throws IOException {
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            byte[] bI = file.getBytes();
            InputStream fis = new ByteArrayInputStream(bI);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bI.length);
            metadata.setContentType("jpg/jpeg/png");
            metadata.setCacheControl("public, max-age=31536000");
            String path = String.join("/", dirPath) + "/" + filename;
            log.info("saving_image: " + path);
            s3.putObject(bucket, path, fis, metadata);
            s3.setObjectAcl(bucket, path, CannedAccessControlList.PublicRead);
        }
    }

    public List<String> getMediaAssets(String clientEmail) {
        return getImageUrls("media-assets", clientEmail);

    }

    public List<String> getPaidAdvertisingReport(String clientEmail, Long reportId) {
        return getImageUrls("paid-advertising-reports", clientEmail, reportId.toString());
    }

    public long getPaidAdvertisingReportSize(String clientEmail, Long reportId) {
        return getImagesSize("paid-advertising-reports", clientEmail, reportId.toString());
    }

    private List<String> getImageUrls(String... dirPath) {
        List<S3ObjectSummary> summaries = getImageSummaries(dirPath);

        return summaries.stream()
                .map(summary -> baseUrl + UriUtils.encode(summary.getKey(), "UTF-8"))
                .collect(Collectors.toList());
    }

    private long getImagesSize(String... dirPath) {
        List<S3ObjectSummary> summaries = getImageSummaries(dirPath);

        return summaries.stream()
                .mapToLong(S3ObjectSummary::getSize)
                .sum();
    }

    private List<S3ObjectSummary> getImageSummaries(String... dirPath) {
        String prefix = String.join("/", dirPath);
        ObjectListing listing = s3.listObjects(bucket, prefix);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            summaries.addAll(listing.getObjectSummaries());
        }
        return summaries;
    }

    public byte[] getMediaAssetsZipped(String clientEmail) throws DownloadingImagesException {
        return getImagesZipped("media-assets", clientEmail);
    }

    public byte[] getPaidAdvertisingReportZipped(String clientEmail, Long reportId) throws DownloadingImagesException {
        return getImagesZipped("paid-advertising-reports", clientEmail, reportId.toString());
    }


    private byte[] getImagesZipped(String... dirPath) throws DownloadingImagesException {
        String prefix = String.join("/", dirPath);
        ObjectListing listing = s3.listObjects(bucket, prefix);
        List<String> keys = new ArrayList<>(listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList());


        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            keys.addAll(listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList());
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (String key : keys) {
                String[] keyPathElements = key.split("/");
                String fileName = keyPathElements[keyPathElements.length - 1];
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);

                S3ObjectInputStream objectContent = s3.getObject(bucket, key).getObjectContent();
                byte[] bytes = new byte[1024];
                int length;
                while ((length = objectContent.read(bytes)) >= 0) {
                    zipOutputStream.write(bytes, 0, length);
                }
                objectContent.close();
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}