package com.example.onboardingservice.service;

import com.example.onboardingservice.exception.UserIsNotClientException;
import com.example.onboardingservice.exception.UserNotFoundException;
import com.example.onboardingservice.exception.WrongListSize;
import com.example.onboardingservice.model.Client;
import com.example.onboardingservice.model.Role;
import com.example.onboardingservice.model.User;
import com.example.onboardingservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> listByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public Client findClientByEmail(String email) throws UserNotFoundException {
        return (Client) userRepository.findByRoleAndEmail(Role.CLIENT, email)
                .orElseThrow(UserNotFoundException::new);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void updateClient(
            String email,
            String fullName,
            List<String> formAnswers,
            List<String> onboardingStages,
            Long activeStage
    ) throws UserNotFoundException, UserIsNotClientException, WrongListSize {
        User user = findByEmail(email);
        if (!(user instanceof Client existing)) {
            throw new UserIsNotClientException();
        }
        if (onboardingStages != null && onboardingStages.size() != 3) {
            throw new WrongListSize();
        }
        if (formAnswers != null && formAnswers.size() != 6) {
            throw new WrongListSize();
        }
        if (fullName != null) existing.setFullName(fullName);
        if (formAnswers != null) existing.setFormAnswers(formAnswers);
        if (onboardingStages != null) existing.setOnboardingStages(onboardingStages);
        if (activeStage != null) existing.setActiveStage(activeStage);
        save(existing);
    }

    public Boolean isFormFilled(String email)
            throws UserNotFoundException, UserIsNotClientException {
        User user = findByEmail(email);
        if (!(user instanceof Client existing)) {
            throw new UserIsNotClientException();
        }
        return existing.getFormAnswers().stream().noneMatch(Objects::isNull);
    }

    public User findByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> findByEmailOptional(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public List<User> deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
        return listByRole(Role.CLIENT);
    }
}
