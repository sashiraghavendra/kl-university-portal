package com.kluniversity.config;

import com.kluniversity.entity.AdminUser;
import com.kluniversity.entity.AdmissionStatus;
import com.kluniversity.entity.Hostel;
import com.kluniversity.entity.HostelRoom;
import com.kluniversity.entity.RoomAvailabilityStatus;
import com.kluniversity.entity.Role;
import com.kluniversity.repository.AdminRepository;
import com.kluniversity.repository.HostelBookingRepository;
import com.kluniversity.repository.HostelRepository;
import com.kluniversity.repository.HostelRoomRepository;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final HostelBookingRepository hostelBookingRepository;
    private final HostelRepository hostelRepository;
    private final HostelRoomRepository hostelRoomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        AdminUser admin = adminRepository.findByUsername("admin")
                .orElse(AdminUser.builder().username("admin").role(Role.ADMIN).build());
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        adminRepository.save(admin);

        studentRepository.findAll().stream()
                .filter(student -> student.getStatus() == null)
                .forEach(student -> {
                    student.setStatus(AdmissionStatus.PENDING);
                    studentRepository.save(student);
                });

        hostelBookingRepository.findAll().stream()
                .filter(booking -> booking.getRoomNumber() == null && booking.getRoom() != null)
                .forEach(booking -> {
                    booking.setRoomNumber(booking.getRoom().getRoomNumber());
                    booking.setBookingStatus("CONFIRMED");
                    booking.setHostelPaymentStatus(booking.getHostelPaymentStatus() == null
                            ? com.kluniversity.entity.PaymentStatus.PENDING
                            : booking.getHostelPaymentStatus());
                    hostelBookingRepository.save(booking);
                });

        seedRoomInventory();
    }

    private void seedRoomInventory() {
        List<String> roomTypes = List.of("1 Sharing AC", "2 Sharing AC", "3 Sharing Non AC", "4 Sharing Non AC");
        hostelRoomRepository.findAll().forEach(room -> {
            if (room.getBedCapacity() == null || room.getBedCapacity() <= 0) {
                room.setBedCapacity(capacityFromRoomType(room.getRoomType()));
            }
            int bookedBeds = Math.toIntExact(hostelBookingRepository.countByRoomRoomId(room.getRoomId()));
            room.setOccupiedBeds(Math.max(bookedBeds, room.getOccupiedBeds() == null ? 0 : room.getOccupiedBeds()));
            room.setAvailabilityStatus(room.availableBeds() > 0 ? RoomAvailabilityStatus.AVAILABLE : RoomAvailabilityStatus.OCCUPIED);
            hostelRoomRepository.save(room);
        });

        for (Hostel hostel : hostelRepository.findAll()) {
            String prefix = hostel.getHostelName().replaceAll("[^A-Za-z]", "").substring(0, Math.min(2, hostel.getHostelName().replaceAll("[^A-Za-z]", "").length())).toUpperCase();
            for (String roomType : roomTypes) {
                long existing = hostelRoomRepository.countByHostelHostelIdAndRoomType(hostel.getHostelId(), roomType);
                int capacity = capacityFromRoomType(roomType);
                BigDecimal fee = feeForRoomType(roomType);
                for (long i = existing + 1; i <= 10; i++) {
                    String roomNumber = prefix + "-" + roomType.charAt(0) + String.format("%02d", i);
                    hostelRoomRepository.save(HostelRoom.builder()
                            .hostel(hostel)
                            .roomType(roomType)
                            .roomNumber(roomNumber)
                            .sharingType(capacity + " Sharing")
                            .acType(roomType.contains("Non AC") ? "NON_AC" : "AC")
                            .roomFee(fee)
                            .bedCapacity(capacity)
                            .occupiedBeds(0)
                            .availabilityStatus(RoomAvailabilityStatus.AVAILABLE)
                            .build());
                }
            }
        }
    }

    private int capacityFromRoomType(String roomType) {
        if (roomType == null || roomType.isBlank()) {
            return 1;
        }
        return Character.isDigit(roomType.trim().charAt(0)) ? Character.getNumericValue(roomType.trim().charAt(0)) : 1;
    }

    private BigDecimal feeForRoomType(String roomType) {
        return switch (roomType) {
            case "1 Sharing AC" -> BigDecimal.valueOf(120000);
            case "2 Sharing AC" -> BigDecimal.valueOf(95000);
            case "3 Sharing Non AC" -> BigDecimal.valueOf(75000);
            default -> BigDecimal.valueOf(60000);
        };
    }
}
