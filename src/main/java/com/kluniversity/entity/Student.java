package com.kluniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_students_email", columnList = "email", unique = true),
        @Index(name = "idx_students_department", columnList = "department")
})
public class Student {
    @Id
    @Column(name = "reg_no", length = 32)
    private String regNo;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String gender;
    private LocalDate dob;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "mother_name")
    private String motherName;

    @Column(nullable = false, length = 10)
    private String mobile;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 1000)
    private String address;

    private String city;
    private String state;
    private String pincode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    private String department;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "student_photo")
    private String studentPhoto;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "admission_status")
    private AdmissionStatus status;
}
