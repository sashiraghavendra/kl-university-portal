package com.kluniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_name", nullable = false)
    private String courseName;

    private String department;
    private String duration;

    @Column(name = "total_fee", nullable = false)
    private BigDecimal totalFee;

    @Column(name = "available_seats")
    private Integer availableSeats;
}
