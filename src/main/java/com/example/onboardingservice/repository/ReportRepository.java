package com.example.onboardingservice.repository;

import com.example.onboardingservice.model.Note;
import com.example.onboardingservice.model.NoteType;
import com.example.onboardingservice.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("select r from Report r where r.recipient.email = :email")
    List<Report> findByRecipient(String email);

}
