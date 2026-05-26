package com.kluniversity.service;

import com.kluniversity.dto.FeePaymentRequest;
import com.kluniversity.entity.Fee;
import com.kluniversity.entity.Payment;
import com.kluniversity.entity.PaymentStatus;
import com.kluniversity.entity.Student;
import com.kluniversity.exception.BusinessException;
import com.kluniversity.exception.ResourceNotFoundException;
import com.kluniversity.repository.FeeRepository;
import com.kluniversity.repository.HostelBookingRepository;
import com.kluniversity.repository.PaymentRepository;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeeService {
    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final HostelBookingRepository hostelBookingRepository;
    private final EmailService emailService;

    @Transactional
    public Payment pay(FeePaymentRequest request) {
        Student student = studentRepository.findById(request.getRegNo())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Fee fee = feeRepository.findByStudentRegNoAndSemester(request.getRegNo(), request.getSemester())
                .orElseThrow(() -> new ResourceNotFoundException("Fee record not found"));
        if (request.getAmount().compareTo(fee.getBalanceFee()) > 0) {
            throw new BusinessException("Payment amount cannot exceed balance fee");
        }
        fee.setPaidFee(fee.getPaidFee().add(request.getAmount()));
        fee.setBalanceFee(fee.getTotalFee().subtract(fee.getPaidFee()));
        fee.setPaymentStatus(fee.getBalanceFee().signum() == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
        hostelBookingRepository.findByStudentRegNo(request.getRegNo()).ifPresent(booking -> {
            booking.setHostelPaymentStatus(fee.getPaymentStatus());
            hostelBookingRepository.save(booking);
        });
        Payment payment = Payment.builder()
                .student(student)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentMethod(request.getPaymentMethod())
                .semester(request.getSemester())
                .build();
        feeRepository.save(fee);
        Payment saved = paymentRepository.save(payment);
        emailService.send(student.getEmail(), "Fee payment successful",
                "Payment received: Rs. " + request.getAmount() + ". Transaction: " + saved.getTransactionId());
        return saved;
    }

    public List<Fee> feeHistory(String regNo) {
        return feeRepository.findByStudentRegNoOrderBySemesterAsc(regNo);
    }

    public List<Payment> paymentHistory(String regNo) {
        return paymentRepository.findByStudentRegNoOrderByPaymentDateDesc(regNo);
    }
}
