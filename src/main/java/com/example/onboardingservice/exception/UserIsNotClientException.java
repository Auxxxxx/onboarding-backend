package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserIsNotClientException extends Exception {
    public UserIsNotClientException(String message) {
        super(message);
    }
}