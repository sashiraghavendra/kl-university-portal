package com.kluniversity.repository;

import com.kluniversity.entity.HostelBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostelBookingRepository extends JpaRepository<HostelBooking, Long> {
    Optional<HostelBooking> findByStudentRegNo(String regNo);
    boolean existsByStudentRegNo(String regNo);
    long countByRoomRoomId(Long roomId);
}
