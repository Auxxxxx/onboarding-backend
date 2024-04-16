package com.example.onboardingservice.repository;

import com.example.onboardingservice.OnboardingServiceApplication;
import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.NoteType;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OnboardingServiceApplication.class)
@ContextConfiguration(initializers = {NoteRepositoryTests.Initializer.class})
public class NoteRepositoryTests {

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:13.1-alpine")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa");

    @Autowired
    private NoteRepository noteRepository;

    @Test
    public void NoteRepository_SaveMeetingNote_ReturnSavedNote() {
        Note note = Note.builder()
                .noteType(NoteType.MEETING_NOTES)
                .recipient(null)
                .date(LocalDate.now())
                .content("meeting_note_content")
                .build();

        Note savedNote = noteRepository.save(note);

        Assertions.assertThat(savedNote).isNotNull();
        Assertions.assertThat(savedNote.getId()).isGreaterThan(0);
    }

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


}
