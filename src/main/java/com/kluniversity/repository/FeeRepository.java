package com.kluniversity.repository;

import com.kluniversity.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByStudentRegNoOrderBySemesterAsc(String regNo);
    Optional<Fee> findByStudentRegNoAndSemester(String regNo, Integer semester);
}
