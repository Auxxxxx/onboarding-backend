package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WrongListSize extends Exception {
    public WrongListSize(String message) {
        super(message);
    }
}