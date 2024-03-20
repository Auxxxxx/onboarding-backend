package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DownloadingImagesException extends Exception {
    public DownloadingImagesException(String message) {
        super(message);
    }
}