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
@Table(name = "hostel_rooms")
public class HostelRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hostel_id")
    private Hostel hostel;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "room_number", unique = true)
    private String roomNumber;

    @Column(name = "sharing_type")
    private String sharingType;

    @Column(name = "ac_type")
    private String acType;

    @Column(name = "room_fee")
    private BigDecimal roomFee;

    @Column(name = "bed_capacity")
    private Integer bedCapacity;

    @Column(name = "occupied_beds")
    private Integer occupiedBeds;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status")
    private RoomAvailabilityStatus availabilityStatus;

    public int availableBeds() {
        int capacity = bedCapacity == null ? 1 : bedCapacity;
        int occupied = occupiedBeds == null ? 0 : occupiedBeds;
        return Math.max(capacity - occupied, 0);
    }
}
