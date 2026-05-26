package com.kluniversity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hostel_bookings")
public class HostelBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reg_no", nullable = false, unique = true)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hostel_id")
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    private HostelRoom room;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "mess_type")
    private String messType;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "hostel_payment_status")
    private PaymentStatus hostelPaymentStatus;

    private LocalDateTime bookedAt;
}
