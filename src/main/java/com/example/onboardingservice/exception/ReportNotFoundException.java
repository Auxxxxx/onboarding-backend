package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReportNotFoundException extends Exception {
    public ReportNotFoundException(String message) {
        super(message);
    }
}