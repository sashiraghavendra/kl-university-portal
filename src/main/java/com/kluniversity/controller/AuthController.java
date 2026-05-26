package com.kluniversity.controller;

import com.kluniversity.dto.LoginRequest;
import com.kluniversity.dto.RegisterRequest;
import com.kluniversity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Object register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@RequestBody Map<String, String> body) {
        return Map.of("message", authService.forgotPassword(body.getOrDefault("username", "")));
    }

    @PostMapping("/reset-password")
    public Map<String, String> reset(@RequestBody Map<String, String> body) {
        return Map.of("message", authService.resetPassword(
                body.getOrDefault("username", ""),
                body.getOrDefault("newPassword", "")));
    }
}
