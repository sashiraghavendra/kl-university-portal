package com.kluniversity.service;

import com.kluniversity.dto.HostelBookingRequest;
import com.kluniversity.entity.*;
import com.kluniversity.exception.BusinessException;
import com.kluniversity.exception.ResourceNotFoundException;
import com.kluniversity.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostelService {
    private final HostelRepository hostelRepository;
    private final HostelRoomRepository roomRepository;
    private final HostelBookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final FeeRepository feeRepository;
    private final EmailService emailService;

    public List<Hostel> hostels() {
        return hostelRepository.findAll();
    }

    public List<HostelRoom> rooms(Long hostelId, String roomType, String sharingType) {
        if (hostelId != null && roomType != null && !roomType.isBlank()) {
            return roomRepository.findByHostelHostelIdAndRoomType(hostelId, roomType);
        }
        if (hostelId == null) {
            return roomRepository.findByAvailabilityStatus(RoomAvailabilityStatus.AVAILABLE);
        }
        return roomRepository.findByHostelHostelIdAndAvailabilityStatus(hostelId, RoomAvailabilityStatus.AVAILABLE);
    }

    public List<HostelRoom> allRooms() {
        return roomRepository.findAll();
    }

    public List<HostelBooking> allocations() {
        return bookingRepository.findAll();
    }

    @Transactional
    public HostelBooking book(HostelBookingRequest request) {
        if (bookingRepository.existsByStudentRegNo(request.getRegNo())) {
            throw new BusinessException("Hostel already booked for this student");
        }
        Student student = studentRepository.findById(request.getRegNo())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (student.getStatus() != AdmissionStatus.APPROVED) {
            throw new BusinessException("Admission must be approved before hostel booking");
        }
        HostelRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        normalizeBeds(room);
        if (room.availableBeds() <= 0) {
            throw new BusinessException("Selected room is full");
        }
        Hostel hostel = room.getHostel();
        if (hostel.getAvailableRooms() == null || hostel.getAvailableRooms() <= 0) {
            throw new BusinessException("No rooms available in selected hostel");
        }
        room.setOccupiedBeds(room.getOccupiedBeds() + 1);
        room.setAvailabilityStatus(room.availableBeds() == 0 ? RoomAvailabilityStatus.OCCUPIED : RoomAvailabilityStatus.AVAILABLE);
        if (room.availableBeds() == 0) {
            hostel.setAvailableRooms(hostel.getAvailableRooms() - 1);
        }
        HostelBooking booking = HostelBooking.builder()
                .student(student)
                .hostel(hostel)
                .room(room)
                .roomNumber(room.getRoomNumber())
                .messType(request.getMessType())
                .bookingStatus("CONFIRMED")
                .hostelPaymentStatus(PaymentStatus.PENDING)
                .bookedAt(LocalDateTime.now())
                .build();
        addHostelFee(student, room.getRoomFee());
        emailService.send(student.getEmail(), "Hostel booking confirmed",
                "Your hostel room has been booked in " + hostel.getHostelName());
        return bookingRepository.save(booking);
    }

    public HostelBooking allocation(String regNo) {
        return bookingRepository.findByStudentRegNo(regNo)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel booking not found"));
    }

    private void addHostelFee(Student student, BigDecimal hostelFee) {
        Fee fee = feeRepository.findByStudentRegNoAndSemester(student.getRegNo(), 1).orElse(null);
        if (fee != null) {
            BigDecimal messFee = BigDecimal.valueOf(45000);
            fee.setHostelFee(hostelFee);
            fee.setMessFee(messFee);
            fee.setTotalFee(fee.getTotalFee().add(hostelFee).add(messFee));
            fee.setBalanceFee(fee.getTotalFee().subtract(fee.getPaidFee()));
            feeRepository.save(fee);
        }
    }

    private void normalizeBeds(HostelRoom room) {
        if (room.getBedCapacity() == null || room.getBedCapacity() <= 0) {
            room.setBedCapacity(capacityFromRoomType(room.getRoomType()));
        }
        if (room.getOccupiedBeds() == null || room.getOccupiedBeds() < 0) {
            room.setOccupiedBeds(0);
        }
    }

    private int capacityFromRoomType(String roomType) {
        if (roomType == null || roomType.isBlank()) {
            return 1;
        }
        char first = roomType.trim().charAt(0);
        return Character.isDigit(first) ? Character.getNumericValue(first) : 1;
    }
}
