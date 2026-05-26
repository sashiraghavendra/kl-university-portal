package com.kluniversity.service;

import com.kluniversity.entity.Course;
import com.kluniversity.exception.ResourceNotFoundException;
import com.kluniversity.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<Course> all() {
        return courseRepository.findAll();
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public Course update(Long id, Course update) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setCourseName(update.getCourseName());
        course.setDepartment(update.getDepartment());
        course.setDuration(update.getDuration());
        course.setTotalFee(update.getTotalFee());
        course.setAvailableSeats(update.getAvailableSeats());
        return courseRepository.save(course);
    }
}
