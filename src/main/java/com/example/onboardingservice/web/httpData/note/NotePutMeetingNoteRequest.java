package com.example.onboardingservice.web.httpData.note;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotePutMeetingNoteRequest {
    private Long id;
    @Schema(example = "line1\n line2")
    private String content;
    @Schema(example = "Useful info")
    private String header;
}