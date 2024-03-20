package com.example.onboardingservice.service;

import com.example.onboardingservice.exception.UserAlreadyExistsException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongPasswordException;
import com.example.onboardingservice.model.Client;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.repository.UserRepository;
import com.example.onboardingservice.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final NoteService noteService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public void register(String fullName, String email, String password) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        Client client = Client.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
        client.setActiveStage(1L);
        client.setOnboardingStages(Arrays.asList("Beginner", "Common client", "Partner"));
        userRepository.save(client);

        try {
            noteService.addDefaultNotes(client);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public User signIn(String email, String password) throws UserNotFoundException, WrongPasswordException {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        boolean validPassword = passwordEncoder.matches(password, user.getPassword());
        if (!validPassword) {
            throw new WrongPasswordException();
        }
        return user;
    }

    public String authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );
        var user = userRepository.findByEmail(email).orElseThrow();
        return jwtService.generateToken(user);
    }
}
