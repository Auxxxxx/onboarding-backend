package com.example.onboardingservice.repository;

import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.NoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("select n from Note n where n.recipient.email = :email and n.noteType = :noteType")
    List<Note> findByRecipientAndNoteType(String email, NoteType noteType);

    @Query("select n from Note n where n.recipient.email = :email and n.noteType = 0 and n.id = :id")
    Optional<Note> findMeetingNoteByRecipientAndId(String email, Long id);

}
