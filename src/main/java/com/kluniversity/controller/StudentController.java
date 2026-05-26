package com.kluniversity.controller;

import com.kluniversity.entity.Student;
import com.kluniversity.entity.AdmissionStatus;
import com.kluniversity.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @GetMapping
    public Object all(@RequestParam(required = false) String search, @PageableDefault(size = 10) Pageable pageable) {
        return studentService.findAll(search, pageable);
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable String id) {
        return studentService.get(id);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable String id, @RequestBody Student student) {
        return studentService.update(id, student);
    }

    @PutMapping("/{id}/status")
    public Student status(@PathVariable String id, @RequestBody Map<String, String> body) {
        return studentService.updateAdmissionStatus(id, AdmissionStatus.valueOf(body.get("status")));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        studentService.delete(id);
    }

    @PostMapping("/{id}/photo")
    public Student photo(@PathVariable String id, @RequestPart MultipartFile file) throws Exception {
        return studentService.uploadPhoto(id, file, uploadDir);
    }
}
