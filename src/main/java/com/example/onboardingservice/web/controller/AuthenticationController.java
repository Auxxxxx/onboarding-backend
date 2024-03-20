package com.example.onboardingservice.web.controller;

import com.example.onboardingservice.exception.UserAlreadyExistsException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongPasswordException;
import com.example.onboardingservice.service.AuthenticationService;
import com.example.onboardingservice.web.httpData.authentication.AuthenticationRegisterRequest;
import com.example.onboardingservice.web.httpData.authentication.AuthenticationSignInRequest;
import com.example.onboardingservice.web.httpData.authentication.AuthenticationSignInResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a client", description = "Register a new client with email, password, and full name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registered successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "409", description = "Conflict. Client with such email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> list(
            @RequestBody(description = "Registration data", required = true)
            @RequestData AuthenticationRegisterRequest request) {
        if (request.getEmail() == null ||
                request.getPassword() == null ||
                request.getFullName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("registering_client: " + request.getEmail());
        try {
            authenticationService.register(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPassword());
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserAlreadyExistsException e) {
            log.error("client_already_exists: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Sign in", description = "Sign in with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed in successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request. Request field is null"),
            @ApiResponse(responseCode = "401", description = "Unauthorized. Wrong password"),
            @ApiResponse(responseCode = "404", description = "Not Found. User with such email is not found")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticationSignInResponse> signIn(
            @RequestBody(description = "Email and password", required = true)
            @RequestData AuthenticationSignInRequest request) {
        if (request.getEmail() == null ||
                request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        log.info("signing_in: " + request.getEmail());
        try {
            var user = authenticationService.signIn(
                    request.getEmail(),
                    request.getPassword());
            var jwt = authenticationService.authenticate(
                    request.getEmail(),
                    request.getPassword());
            var response = AuthenticationSignInResponse.builder()
                    .user(user)
                    .role(user.getRole())
                    .jwt(jwt)
                    .build();
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            log.error("user_not_found: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (WrongPasswordException e) {
            log.error("wrong_password: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}