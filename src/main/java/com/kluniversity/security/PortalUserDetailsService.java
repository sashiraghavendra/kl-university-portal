package com.kluniversity.security;

import com.kluniversity.entity.AdminUser;
import com.kluniversity.entity.Student;
import com.kluniversity.repository.AdminRepository;
import com.kluniversity.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalUserDetailsService implements UserDetailsService {
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return studentRepository.findById(username)
                .map(this::studentDetails)
                .or(() -> studentRepository.findByEmail(username).map(this::studentDetails))
                .or(() -> adminRepository.findByUsername(username).map(this::adminDetails))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private UserDetails studentDetails(Student student) {
        return new User(student.getRegNo(), student.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + student.getRole().name())));
    }

    private UserDetails adminDetails(AdminUser admin) {
        return new User(admin.getUsername(), admin.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())));
    }
}
