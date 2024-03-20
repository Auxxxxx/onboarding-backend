package com.example.onboardingservice.repository;

import com.example.onboardingservice.model.Role;
import com.example.onboardingservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.role = :role")
    List<User> findByRole(@Param("role") Role role);

    @Query("select u from User u where u.role = :role and u.email = :email")
    Optional<User> findByRoleAndEmail(@Param("role") Role role, @Param("email") String email);

    void deleteByEmail(String email);
}
