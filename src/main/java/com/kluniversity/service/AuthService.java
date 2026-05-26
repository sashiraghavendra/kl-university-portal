package com.kluniversity.service;

import com.kluniversity.dto.AuthResponse;
import com.kluniversity.dto.LoginRequest;
import com.kluniversity.dto.RegisterRequest;
import com.kluniversity.entity.*;
import com.kluniversity.exception.BusinessException;
import com.kluniversity.exception.ResourceNotFoundException;
import com.kluniversity.repository.CourseRepository;
import com.kluniversity.repository.FeeRepository;
import com.kluniversity.repository.StudentRepository;
import com.kluniversity.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final FeeRepository feeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered");
        }
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (course.getAvailableSeats() == null || course.getAvailableSeats() <= 0) {
            throw new BusinessException("No seats available for selected course");
        }
        course.setAvailableSeats(course.getAvailableSeats() - 1);
        String regNo = "KLU" + LocalDate.now().getYear() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Student student = Student.builder()
                .regNo(regNo)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .dob(request.getDob())
                .fatherName(request.getFatherName())
                .motherName(request.getMotherName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .course(course)
                .department(course.getDepartment())
                .admissionDate(LocalDate.now())
                .role(Role.STUDENT)
                .status(AdmissionStatus.PENDING)
                .build();
        studentRepository.save(student);
        createFee(student, 1, course.getTotalFee());
        emailService.send(student.getEmail(), "KL University registration received",
                "Your application was submitted. Register number: " + regNo);
        return AuthResponse.builder()
                .role("STUDENT")
                .displayName(student.getFirstName() + " " + student.getLastName())
                .regNo(regNo)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        return studentRepository.findById(request.getUsername())
                .or(() -> studentRepository.findByEmail(request.getUsername()))
                .map(student -> {
                    if (student.getStatus() == AdmissionStatus.PENDING) {
                        throw new BusinessException("Admission is pending admin approval");
                    }
                    if (student.getStatus() == AdmissionStatus.REJECTED) {
                        throw new BusinessException("Admission was rejected. Please contact admissions office");
                    }
                    return tokenFor(student.getRegNo(), "STUDENT", student.getFirstName() + " " + student.getLastName(), student.getRegNo());
                })
                .orElseGet(() -> tokenFor(request.getUsername(), "ADMIN", request.getUsername(), null));
    }

    public String forgotPassword(String username) {
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        emailService.send(username, "KL University password reset OTP", "Your OTP is " + otp);
        return "Password reset OTP sent if the account exists";
    }

    @Transactional
    public String resetPassword(String username, String newPassword) {
        Student student = studentRepository.findById(username)
                .or(() -> studentRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);
        emailService.send(student.getEmail(), "KL University password changed",
                "Your portal password was reset successfully.");
        return "Password reset successful";
    }

    private void createFee(Student student, int semester, BigDecimal courseFee) {
        BigDecimal exam = BigDecimal.valueOf(6000);
        BigDecimal lab = BigDecimal.valueOf(9000);
        BigDecimal library = BigDecimal.valueOf(4000);
        BigDecimal tuition = courseFee.subtract(exam).subtract(lab).subtract(library);
        Fee fee = Fee.builder()
                .student(student)
                .semester(semester)
                .tuitionFee(tuition)
                .examFee(exam)
                .labFee(lab)
                .libraryFee(library)
                .hostelFee(BigDecimal.ZERO)
                .messFee(BigDecimal.ZERO)
                .totalFee(courseFee)
                .paidFee(BigDecimal.ZERO)
                .balanceFee(courseFee)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        feeRepository.save(fee);
    }

    private AuthResponse tokenFor(String username, String role, String name, String regNo) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(userDetails, Map.of("role", role, "regNo", regNo == null ? "" : regNo));
        return AuthResponse.builder().token(token).role(role).displayName(name).regNo(regNo).build();
    }
}
