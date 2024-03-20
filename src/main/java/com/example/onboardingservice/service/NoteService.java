package com.example.onboardingservice.service;

import com.example.onboardingservice.exception.JsonTooLongException;
import com.example.onboardingservice.exception.NoteNotFoundException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongListSize;
import com.example.onboardingservice.model.Client;
import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.NoteType;
import com.example.onboardingservice.repository.NoteRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserService userService;

    public List<Note> listMeetingNotes(String email) {
        return noteRepository.findByRecipientAndNoteType(email, NoteType.MEETING_NOTES);
    }

    public Note findMeetingNoteById(String email, Long noteId) throws NoteNotFoundException {
        return noteRepository.findMeetingNoteByRecipientAndId(email, noteId).orElseThrow(NoteNotFoundException::new);
    }

    public Note getUsefulInfo(String email) throws JsonTooLongException {
        List<Note> found = noteRepository.findByRecipientAndNoteType(email, NoteType.USEFUL_INFO);
        return found.stream().findAny().orElseThrow(JsonTooLongException::new);
    }

    public Note getContactDetails(String email) throws JsonTooLongException {
        List<Note> found = noteRepository.findByRecipientAndNoteType(email, NoteType.CONTACT_DETAILS);
        return found.stream().findAny().orElseThrow(JsonTooLongException::new);
    }

    public void saveMeetingNote(Long id,
                                String content,
                                String header,
                                String recipientEmail) throws UserNotFoundException {
        Client recipient = (Client) userService.findByEmail(recipientEmail);
        Note note = Note.builder()
                .recipient(recipient)
                .header(header)
                .content(content)
                .date(LocalDate.now())
                .noteType(NoteType.MEETING_NOTES)
                .build();
        if (id != null) {
            note.setId(id);
        }
        noteRepository.save(note);
    }

    public void saveUsefulInfo(String recipientEmail,
                               String content) throws UserNotFoundException {
        Note existing = noteRepository.findByRecipientAndNoteType(
                        recipientEmail, NoteType.USEFUL_INFO)
                .stream()
                .findAny()
                .orElseThrow(UserNotFoundException::new);
        existing.setContent(content);
        noteRepository.save(existing);
    }

    public void saveContactDetails(String recipientEmail,
                                   String content) throws UserNotFoundException {
        Note existing = noteRepository.findByRecipientAndNoteType(
                        recipientEmail, NoteType.CONTACT_DETAILS)
                .stream()
                .findAny()
                .orElseThrow(UserNotFoundException::new);
        existing.setContent(content);
        noteRepository.save(existing);
    }

    private void save(Note note) {
        noteRepository.save(note);
    }

    public void addDefaultNotes(Client client) throws UserNotFoundException {
        NoteType[] types = NoteType.values();
        for (NoteType type : types) {
            Note note = buildDefaultNote(type, client);
            save(note);
        }
    }

    public Note buildDefaultNote(NoteType type, Client client) {
        return Note.builder()
                .header(type.getDefaultHeader())
                .content(type.getDefaultContent())
                .noteType(type)
                .recipient(client)
                .date(LocalDate.now())
                .build();
    }

    @Transactional
    public List<Note> deleteMeetingNoteById(Long id)
            throws JsonTooLongException, WrongListSize {
        Note note = noteRepository.findById(id).orElseThrow(JsonTooLongException::new);
        if (note.getNoteType() == NoteType.MEETING_NOTES) {
            noteRepository.deleteById(id);
            return listMeetingNotes(note.getRecipient().getEmail());
        } else {
            throw new WrongListSize();
        }
    }
}
