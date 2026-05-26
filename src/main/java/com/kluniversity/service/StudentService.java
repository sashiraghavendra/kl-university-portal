package com.kluniversity.service;

import com.kluniversity.entity.Student;
import com.kluniversity.entity.AdmissionStatus;
import com.kluniversity.exception.ResourceNotFoundException;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    public Page<Student> findAll(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return studentRepository.findAll(pageable);
        }
        return studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrRegNoContainingIgnoreCase(
                search, search, search, pageable);
    }

    public Student get(String regNo) {
        return studentRepository.findById(regNo).orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    public Student update(String regNo, Student update) {
        Student student = get(regNo);
        student.setMobile(update.getMobile());
        student.setAddress(update.getAddress());
        student.setCity(update.getCity());
        student.setState(update.getState());
        student.setPincode(update.getPincode());
        student.setStatus(update.getStatus() == null ? student.getStatus() : update.getStatus());
        return studentRepository.save(student);
    }

    public Student updateAdmissionStatus(String regNo, AdmissionStatus status) {
        Student student = get(regNo);
        student.setStatus(status);
        return studentRepository.save(student);
    }

    public void delete(String regNo) {
        studentRepository.delete(get(regNo));
    }

    public Student uploadPhoto(String regNo, MultipartFile file, String uploadDir) throws IOException {
        Student student = get(regNo);
        Files.createDirectories(Path.of(uploadDir));
        String filename = regNo + "-" + file.getOriginalFilename();
        Path target = Path.of(uploadDir, filename);
        file.transferTo(target);
        student.setStudentPhoto(target.toString());
        return studentRepository.save(student);
    }
}
