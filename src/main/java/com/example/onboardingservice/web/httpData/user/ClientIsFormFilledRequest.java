package com.example.onboardingservice.web.httpData.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientIsFormFilledRequest {
    @Schema(example = "bill_edwards@gmail.com")
    private String email;
}