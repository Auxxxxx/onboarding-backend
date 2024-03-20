package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JsonTooLongException extends Exception {
    public JsonTooLongException(String message) {
        super(message);
    }
}