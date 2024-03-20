package com.example.onboardingservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class Client extends User {
    @Schema(example = "Bill Edwards")
    private String fullName;

    @Schema(example = "[\"answer 1\",\"answer 2\",...]")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "form_answers", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "form_answers", nullable = false)
    @Size(max = 6)
    private List<String> formAnswers;

    @Schema(example = "[\"stage 1\",\"stage 2\",...]")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "onboarding_stages", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "onboarding_stages", nullable = false)
    @Size(max = 3)
    private List<String> onboardingStages;

    @Schema(example = "1")
    private Long activeStage;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "recipient", fetch=FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    {
        setRole(Role.CLIENT);
    }

}
