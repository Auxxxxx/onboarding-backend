package com.example.onboardingservice.web.controller;

import com.example.onboardingservice.exception.UserIsNotClientException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongListSize;
import com.example.onboardingservice.model.Client;
import com.example.onboardingservice.model.Role;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.service.UserService;
import com.example.onboardingservice.web.httpData.user.*;
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

import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/client", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Tag(name = "Client", description = "Endpoints for CRUD operations on clients")
public class ClientController {
    private final UserService userService;

    @Secured("MANAGER")
    @Operation(summary = "List clients", description = "Lists all clients in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER")
    })
    @GetMapping("/list")
    public ResponseEntity<ClientListResponse> list() {
        log.info("returning_clients_list");
        var users = userService.listByRole(Role.CLIENT);
        var response = ClientListResponse.builder()
                .clients(users.stream().map(u -> (Client) u).collect(Collectors.toList()))
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get client data", description = "Get client data by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "404", description = "Not Found. Client with such email not found")
    })
    @GetMapping("/get-data/{clientEmail}")
    public ResponseEntity<ClientGetDataResponse> get(
            @RequestBody(description = "Client email", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(clientEmail)) {
            log.error("returning_client: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("returning_client: " + clientEmail);
        try {
            var client = userService.findClientByEmail(clientEmail);
            var response = ClientGetDataResponse.builder()
                    .fullName(client.getFullName())
                    .formAnswers(client.getFormAnswers())
                    .onboardingStages(client.getOnboardingStages())
                    .activeStage(client.getActiveStage())
                    .build();
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            log.error("user_not_found: " + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Operation(summary = "Save form", description = "Accepts client data from the form. " +
            "Only fields with a non-null value passed are updated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Either " +
                    "the user with such email is not a client " +
                    "or list size for answers/stages is wrong " +
                    "or email field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to access another client's data"),
            @ApiResponse(responseCode = "404", description = "Not Found. Client with such email not found")
    })
    @PostMapping("/{clientEmail}")
    public ResponseEntity<Void> update(
            @RequestBody(description = "Client data from form " +
                    "Only fields with a non-null value passed are updated", required = true)
            @RequestData ClientPostRequest request,
            @PathVariable("clientEmail") String clientEmail) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (clientEmail == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(clientEmail)) {
            log.error("returning_client: " + clientEmail + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("updating_client: " + clientEmail);
        try {
            userService.updateClient(
                    clientEmail,
                    request.getFullName(),
                    request.getFormAnswers(),
                    request.getOnboardingStages(),
                    request.getActiveStage());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserNotFoundException e) {
            log.error("user_not_found" + clientEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UserIsNotClientException e) {
            log.error("user_is_not_client: " + clientEmail);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (WrongListSize e) {
            log.error("wrong_list_size: " + clientEmail);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Is form filled", description = "Returns true if form has already " +
            "been filled by this client, false otherwise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched result successfully"),
            @ApiResponse(responseCode = "400", description = "Error. " +
                    "The user with such email is not a client " +
                    "or email field is null"),
            @ApiResponse(responseCode = "403", description = "Forbidden. A client is trying to get another client's data"),
            @ApiResponse(responseCode = "404", description = "Error. Client with such email not found")
    })
    @PostMapping("/is-form-filled")
    public ResponseEntity<ClientIsFormFilledResponse> isFormFilled(
            @RequestBody(description = "Client email", required = true)
            @RequestData ClientIsFormFilledRequest request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (request.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (user.getRole() == Role.CLIENT && !user.getEmail().equals(request.getEmail())) {
            log.error("requesting_if_form_filled: " + request.getEmail() + " by: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("requesting_if_form_filled: " + request.getEmail());
        try {
            var isFormFilled = userService.isFormFilled(request.getEmail());
            var response = ClientIsFormFilledResponse.builder()
                    .isFormFilled(isFormFilled)
                    .build();
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            log.error("user_not_found" + request.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UserIsNotClientException e) {
            log.error("user_is_not_client: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Secured("MANAGER")
    @Operation(summary = "Delete client", description = "Deletes client by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden. Accessible only for MANAGER"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
    })
    @DeleteMapping("{clientEmail}")
    public ResponseEntity<ClientDeleteResponse> delete(
            @RequestBody(description = "Email of the client to delete", required = true)
            @PathVariable("clientEmail") String clientEmail) {
        if (clientEmail == null || clientEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("deleting_client: " + clientEmail);
        var clients = userService.deleteByEmail(clientEmail);
        var response = ClientDeleteResponse.builder()
                .clients(clients)
                .build();
        return ResponseEntity.ok(response);
    }

}