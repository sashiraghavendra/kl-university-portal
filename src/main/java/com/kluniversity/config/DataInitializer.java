package com.kluniversity.config;

import com.kluniversity.entity.AdminUser;
import com.kluniversity.entity.AdmissionStatus;
import com.kluniversity.entity.Announcement;
import com.kluniversity.entity.Course;
import com.kluniversity.entity.Department;
import com.kluniversity.entity.Hostel;
import com.kluniversity.entity.HostelRoom;
import com.kluniversity.entity.RoomAvailabilityStatus;
import com.kluniversity.entity.Role;
import com.kluniversity.repository.AdminRepository;
import com.kluniversity.repository.AnnouncementRepository;
import com.kluniversity.repository.CourseRepository;
import com.kluniversity.repository.DepartmentRepository;
import com.kluniversity.repository.HostelBookingRepository;
import com.kluniversity.repository.HostelRepository;
import com.kluniversity.repository.HostelRoomRepository;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final HostelBookingRepository hostelBookingRepository;
    private final HostelRepository hostelRepository;
    private final HostelRoomRepository hostelRoomRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedDepartments();
        seedCourses();
        seedHostels();
        seedAnnouncements();

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

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }
        departmentRepository.saveAll(List.of(
                Department.builder().departmentName("CSE").hodName("Dr. Kavya Rao").build(),
                Department.builder().departmentName("AI & DS").hodName("Dr. Arjun Menon").build(),
                Department.builder().departmentName("IT").hodName("Dr. Sneha Reddy").build(),
                Department.builder().departmentName("ECE").hodName("Dr. Meera Nair").build(),
                Department.builder().departmentName("Mechanical").hodName("Dr. Vikram Shah").build(),
                Department.builder().departmentName("Civil").hodName("Dr. Nandini Iyer").build()
        ));
    }

    private void seedCourses() {
        if (courseRepository.count() > 0) {
            return;
        }
        courseRepository.saveAll(List.of(
                Course.builder().courseName("B.Tech Computer Science Engineering").department("CSE").duration("4 Years").totalFee(BigDecimal.valueOf(125000)).availableSeats(120).build(),
                Course.builder().courseName("B.Tech Artificial Intelligence and Data Science").department("AI & DS").duration("4 Years").totalFee(BigDecimal.valueOf(140000)).availableSeats(90).build(),
                Course.builder().courseName("B.Tech Information Technology").department("IT").duration("4 Years").totalFee(BigDecimal.valueOf(115000)).availableSeats(100).build(),
                Course.builder().courseName("B.Tech Electronics and Communication").department("ECE").duration("4 Years").totalFee(BigDecimal.valueOf(110000)).availableSeats(110).build(),
                Course.builder().courseName("B.Tech Mechanical Engineering").department("Mechanical").duration("4 Years").totalFee(BigDecimal.valueOf(95000)).availableSeats(80).build(),
                Course.builder().courseName("B.Tech Civil Engineering").department("Civil").duration("4 Years").totalFee(BigDecimal.valueOf(90000)).availableSeats(75).build()
        ));
    }

    private void seedHostels() {
        if (hostelRepository.count() > 0) {
            return;
        }
        hostelRepository.saveAll(List.of(
                Hostel.builder().hostelName("Krishna Bhavan").genderType("MALE").totalRooms(120).availableRooms(120).build(),
                Hostel.builder().hostelName("Vivekananda Block").genderType("MALE").totalRooms(100).availableRooms(100).build(),
                Hostel.builder().hostelName("APJ Abdul Kalam Hostel").genderType("MALE").totalRooms(90).availableRooms(90).build(),
                Hostel.builder().hostelName("Lotus Block").genderType("FEMALE").totalRooms(110).availableRooms(110).build(),
                Hostel.builder().hostelName("Saraswati Hostel").genderType("FEMALE").totalRooms(100).availableRooms(100).build()
        ));
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) {
            return;
        }
        announcementRepository.saveAll(List.of(
                Announcement.builder().title("Admissions Open").description("B.Tech admissions for the academic year are now open. Apply through the portal.").postedDate(LocalDateTime.now()).build(),
                Announcement.builder().title("Semester Fee Window").description("Students can pay semester fees online before the due date to avoid late fees.").postedDate(LocalDateTime.now()).build()
        ));
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
