package com.example.onboardingservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NoteCannotBeDeletedException extends Exception {
    public NoteCannotBeDeletedException(String message) {
        super(message);
    }
}