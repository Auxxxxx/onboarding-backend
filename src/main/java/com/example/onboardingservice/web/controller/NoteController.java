package com.example.onboardingservice.web.controller;

import com.example.onboardingservice.exception.JsonTooLongException;
import com.example.onboardingservice.exception.NoteNotFoundException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongListSize;
import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.service.NoteService;
import com.example.onboardingservice.web.httpData.note.*;
import com.example.onboardingservice.web.util.RequestData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/note", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Tag(name = "Note", description = "Endpoints for CRUD operations on notes")
public class NoteController {
    private final NoteService noteService;

    @Operation(summary = "Get meeting notes", description = "Lists all meeting notes of the client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null")
    })
    @GetMapping("/meeting-notes/{clientEmail}")
    public ResponseEntity<NoteGetMeetingNotesResponse> getMeetingNotes(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("returning_meeting_notes: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_meeting_notes: " + clientEmail);
        var meetingNotes = noteService.listMeetingNotes(clientEmail);
        var response = NoteGetMeetingNotesResponse.builder()
                .meetingNotes(meetingNotes)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get meeting note by id", description = "Returns a meeting note by email and id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null")
    })
    @GetMapping("/meeting-notes/{clientEmail}/{noteId}")
    public ResponseEntity<NoteGetMeetingNoteByIdResponse> getMeetingNoteById(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail,
            @RequestBody(description = "Note id", required = true)
            @PathVariable("noteId") Long noteId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("returning_meeting_note_by_id: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_meeting_note_by_id: " + clientEmail);
        try {
            var meetingNote = noteService.findMeetingNoteById(clientEmail, noteId);
            var response = NoteGetMeetingNoteByIdResponse.builder()
                    .meetingNote(meetingNote)
                    .build();
            return ResponseEntity.ok(response);
        } catch (NoteNotFoundException e) {
            log.error("note_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get useful info", description = "Gets useful info of the client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "404", description = "Not Found. This client does not have useful info")
    })
    @GetMapping("/useful-info/{clientEmail}")
    public ResponseEntity<NoteGetUsefulInfoResponse> getUsefulInfo(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("returning_useful_info: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_useful_info: " + clientEmail);
        try {
            var usefulInfo = noteService.getUsefulInfo(clientEmail);
            var response = NoteGetUsefulInfoResponse.builder()
                    .usefulInfo(usefulInfo)
                    .build();
            return ResponseEntity.ok(response);
        } catch (JsonTooLongException e) {
            log.error("useful_info_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Get contact details", description = "Gets contact detail info of the client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "404", description = "Error. This client does not have contact details")
    })
    @GetMapping("/contact-details/{clientEmail}")
    public ResponseEntity<NoteGetContactDetailsResponse> getContactDetails(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!user.getEmail().equals(clientEmail)) {
            log.error("returning_contact_details: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_contact_details: " + clientEmail);
        try {
            var contactDetails = noteService.getContactDetails(clientEmail);
            var response = NoteGetContactDetailsResponse.builder()
                    .contactDetails(contactDetails)
                    .build();
            return ResponseEntity.ok(response);
        } catch (JsonTooLongException e) {
            log.error("contact_details_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Save a meeting note", description = "Saves the meeting note created or edited by the manager.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "404", description = "Not Found. The recipient client is not found")
    })
    @PutMapping("/meeting-notes/{recipientEmail}")
    public ResponseEntity<Void> putMeetingNote(
            @RequestBody(description = """
                    Data of the note to be added
                    If noteId is specified - the note with this id is edited.
                    If noteId is null - a new one is saved
                    Content strings must be not null
                    """, required = true)
            @RequestData NotePutMeetingNoteRequest request,
            @PathVariable("recipientEmail") String recipientEmail) {
        if (recipientEmail == null ||
                request.getHeader() == null ||
                request.getContent() == null ||
                request.getContent().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("saving_meeting_note: " + recipientEmail);
        try {
            noteService.saveMeetingNote(
                    request.getId(),
                    request.getContent(),
                    request.getHeader(),
                    recipientEmail);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserNotFoundException e) {
            log.error("recipient_not_found: " + recipientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Save useful info",
            description = "Saves the useful info edited by the manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "404", description = "Error. The recipient client is not found")
    })
    @PutMapping("/useful-info/{recipientEmail}")
    public ResponseEntity<Void> putUsefulInfo(
            @RequestBody(description = "Data of the note to be edited. " +
                    "Content strings must be not null", required = true)
            @RequestData NotePutUsefulInfoRequest request,
            @PathVariable("recipientEmail") String recipientEmail) {
        if (recipientEmail == null ||
                request.getContent() == null ||
                request.getContent().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("saving_useful_info: " + recipientEmail);
        try {
            noteService.saveUsefulInfo(
                    recipientEmail,
                    request.getContent());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserNotFoundException e) {
            log.error("recipient_not_found: " + recipientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Save contact details",
            description = "Saves the contact details edited by the manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "404", description = "Error. The recipient client is not found")
    })
    @PutMapping("/contact-details/{recipientEmail}")
    public ResponseEntity<Void> putContactDetails(
            @RequestBody(description = "Data of the note to be edited. " +
                    "Content strings must be not null", required = true)
            @RequestData NotePutContactDetailsRequest request,
            @PathVariable("recipientEmail") String recipientEmail) {
        if (recipientEmail == null ||
                request.getContent() == null ||
                request.getContent().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("saving_contact_details: " + recipientEmail);
        try {
            noteService.saveContactDetails(
                    recipientEmail,
                    request.getContent());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserNotFoundException e) {
            log.error("recipient_not_found: " + recipientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Delete meeting note", description = "Deletes the meeting note by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Error. " +
                    "This type of note cannot be deleted " +
                    "or a request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "404", description = "Error. The note to delete is not found")

    })
    @DeleteMapping("/meeting-note/{id}")
    public ResponseEntity<NoteDeleteMeetingNoteResponse> deleteMeetingNote(
            @RequestBody(description = "Id of the meeting note to delete", required = true)
            @PathVariable("id") Long id) {
        if (id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("deleting_meeting_note: " + id);
        try {
            var meetingNotes = noteService.deleteMeetingNoteById(id);
            var response = NoteDeleteMeetingNoteResponse.builder()
                    .meetingNotes(meetingNotes)
                    .build();
            return ResponseEntity.ok(response);
        } catch (JsonTooLongException e) {
            log.error("meeting_note_not_found: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (WrongListSize e) {
            log.error("note_cannot_be_deleted: " + id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}