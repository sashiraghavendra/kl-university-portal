package com.kluniversity.controller;

import com.kluniversity.entity.Announcement;
import com.kluniversity.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;

    @GetMapping
    public Object all() {
        return announcementService.all();
    }

    @PostMapping
    public Announcement post(@RequestBody Announcement announcement) {
        return announcementService.post(announcement);
    }
}
