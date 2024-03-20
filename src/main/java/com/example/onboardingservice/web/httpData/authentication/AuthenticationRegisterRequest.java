package com.example.onboardingservice.web.httpData.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationRegisterRequest {
    @Schema(example = "bill_edwards@gmail.com")
    private String email;
    @Schema(example = "cookie12345")
    private String password;
    @Schema(example = "Bill Edwards")
    private String fullName;

}
