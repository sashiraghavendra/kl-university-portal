package com.kluniversity.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String fatherName;
    private String motherName;
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;
    @Email
    @NotBlank
    private String email;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "Password must be at least 8 characters with upper, lower, number and special character")
    private String password;
    private String address;
    private String city;
    private String state;
    private String pincode;
    @NotNull
    private Long courseId;
}
