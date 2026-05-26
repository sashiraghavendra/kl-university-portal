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
@Table(name = "fees")
public class Fee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_id")
    private Long feeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reg_no", nullable = false)
    private Student student;

    private Integer semester;

    @Column(name = "tuition_fee")
    private BigDecimal tuitionFee;

    @Column(name = "exam_fee")
    private BigDecimal examFee;

    @Column(name = "lab_fee")
    private BigDecimal labFee;

    @Column(name = "library_fee")
    private BigDecimal libraryFee;

    @Column(name = "hostel_fee")
    private BigDecimal hostelFee;

    @Column(name = "mess_fee")
    private BigDecimal messFee;

    @Column(name = "total_fee")
    private BigDecimal totalFee;

    @Column(name = "paid_fee")
    private BigDecimal paidFee;

    @Column(name = "balance_fee")
    private BigDecimal balanceFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
}
