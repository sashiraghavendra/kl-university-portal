package com.kluniversity.repository;

import com.kluniversity.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(
            String firstName, String lastName, String regNo, Pageable pageable);
}
