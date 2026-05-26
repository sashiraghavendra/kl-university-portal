package com.kluniversity.service;

import com.kluniversity.entity.Announcement;
import com.kluniversity.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public List<Announcement> all() {
        return announcementRepository.findAllByOrderByPostedDateDesc();
    }

    public Announcement post(Announcement announcement) {
        announcement.setPostedDate(LocalDateTime.now());
        return announcementRepository.save(announcement);
    }
}
