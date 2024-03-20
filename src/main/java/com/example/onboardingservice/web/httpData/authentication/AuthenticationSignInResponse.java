package com.example.onboardingservice.web.httpData.authentication;

import com.example.onboardingservice.model.Role;
import com.example.onboardingservice.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationSignInResponse {
    private String jwt;
    private User user;
    private Role role;
}
