package com.example.onboardingservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Table(name = "note")
public class Note implements Serializable {
    @Id
    @GeneratedValue
    @ToString.Include
    private Long id;
    @JsonIgnore
    private NoteType noteType;
    @Schema(example = "Meeting in November")
    private String header;
    @Schema(example = "yyyy-MM-dd")
    private LocalDate date;
    private String content;
    @JsonIgnore
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private Client recipient;
}
