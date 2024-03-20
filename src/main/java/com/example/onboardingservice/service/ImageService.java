package com.example.onboardingservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.onboardingservice.exception.DownloadingImagesException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ImageService {
    @Value("${storage.base-url}")
    private String baseUrl;
    @Value("${storage.root}")
    private String bucket;
    private final AmazonS3 s3;

    public void uploadMediaAssets(MultipartFile[] files, String clientEmail) throws IOException {
        saveImages(files, clientEmail, "media-assets");
    }

    public void uploadPaidAdvertisingReports(MultipartFile[] files, String clientEmail) throws IOException {
        saveImages(files, clientEmail, "paid-advertising-reports");
    }

    private void saveImages(MultipartFile[] files, String clientEmail, String folder) throws IOException {
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            byte[] bI = file.getBytes();
            InputStream fis = new ByteArrayInputStream(bI);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bI.length);
            metadata.setContentType("jpg/jpeg/png");
            metadata.setCacheControl("public, max-age=31536000");
            String path = String.join("/", folder, clientEmail, filename);
            s3.putObject(bucket, path, fis, metadata);
            s3.setObjectAcl(bucket, path, CannedAccessControlList.PublicRead);
        }
    }

    public List<String> getMediaAssets(String clientEmail) {
        return getImages(clientEmail, "media-assets");

    }

    public List<String> getPaidAdvertisingReports(String clientEmail) {
        return getImages(clientEmail, "paid-advertising-reports");
    }

    private List<String> getImages(String clientEmail, String folder) {
        String prefix = String.join("/", folder, clientEmail);
        ObjectListing listing = s3.listObjects(bucket, prefix);
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();

        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            summaries.addAll(listing.getObjectSummaries());
        }
        return summaries.stream()
                .map(summary -> baseUrl + summary.getKey())
                .collect(Collectors.toList());
    }

    public byte[] getMediaAssetsZipped(String clientEmail) throws DownloadingImagesException {
        return getImagesZipped("media-assets", clientEmail);
    }

    public byte[] getPaidAdvertisingReportsZipped(String clientEmail) throws DownloadingImagesException {
        return getImagesZipped("paid-advertising-reports", clientEmail);
    }


    private byte[] getImagesZipped(String folder, String clientEmail) throws DownloadingImagesException {
        String prefix = String.join("/", folder, clientEmail);
        ObjectListing listing = s3.listObjects(bucket, prefix);
        List<String> keys = new ArrayList<>(listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList());


        while (listing.isTruncated()) {
            listing = s3.listNextBatchOfObjects(listing);
            keys.addAll(listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList());
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (String key : keys) {
                ZipEntry zipEntry = new ZipEntry(key);
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