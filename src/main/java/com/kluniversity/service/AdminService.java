package com.kluniversity.service;

import com.kluniversity.dto.DashboardStats;
import com.kluniversity.entity.Fee;
import com.kluniversity.entity.PaymentStatus;
import com.kluniversity.entity.RoomAvailabilityStatus;
import com.kluniversity.repository.CourseRepository;
import com.kluniversity.repository.FeeRepository;
import com.kluniversity.repository.HostelRoomRepository;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final FeeRepository feeRepository;
    private final HostelRoomRepository roomRepository;

    public DashboardStats stats() {
        var students = studentRepository.findAll();
        var fees = feeRepository.findAll();
        BigDecimal collected = fees.stream()
                .map(Fee::getPaidFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> departmentWise = students.stream()
                .collect(Collectors.groupingBy(s -> s.getDepartment() == null ? "Unassigned" : s.getDepartment(), Collectors.counting()));
        Map<String, Long> paymentStatus = fees.stream()
                .collect(Collectors.groupingBy(f -> f.getPaymentStatus() == null ? PaymentStatus.PENDING.name() : f.getPaymentStatus().name(), Collectors.counting()));
        return DashboardStats.builder()
                .totalStudents(students.size())
                .totalAdmissions(students.size())
                .totalCourses(courseRepository.count())
                .occupiedRooms(roomRepository.findByAvailabilityStatus(RoomAvailabilityStatus.OCCUPIED).size())
                .totalFeeCollected(collected)
                .departmentWiseStudents(departmentWise)
                .paymentStatus(paymentStatus)
                .build();
    }
}
