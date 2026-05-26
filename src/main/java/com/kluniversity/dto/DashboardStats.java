package com.kluniversity.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalStudents;
    private long totalAdmissions;
    private long totalCourses;
    private long occupiedRooms;
    private BigDecimal totalFeeCollected;
    private Map<String, Long> departmentWiseStudents;
    private Map<String, Long> paymentStatus;
}
