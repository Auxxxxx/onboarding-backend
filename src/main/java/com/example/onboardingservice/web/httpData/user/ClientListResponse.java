package com.example.onboardingservice.web.httpData.user;

import com.example.onboardingservice.model.Client;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientListResponse {
    private List<Client> clients;
}

