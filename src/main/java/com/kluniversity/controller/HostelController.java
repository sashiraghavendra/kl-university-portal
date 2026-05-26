package com.kluniversity.controller;

import com.kluniversity.dto.HostelBookingRequest;
import com.kluniversity.entity.HostelBooking;
import com.kluniversity.service.HostelService;
import com.kluniversity.util.PdfGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hostel")
@RequiredArgsConstructor
public class HostelController {
    private final HostelService hostelService;
    private final PdfGenerator pdfGenerator;

    @GetMapping("/list")
    public Object hostels() {
        return hostelService.hostels();
    }

    @GetMapping("/rooms")
    public Object rooms(@RequestParam(required = false) Long hostelId,
                        @RequestParam(required = false) String roomType,
                        @RequestParam(required = false) String sharingType) {
        return hostelService.rooms(hostelId, roomType, sharingType);
    }

    @GetMapping("/rooms/all")
    public Object allRooms() {
        return hostelService.allRooms();
    }

    @GetMapping("/allocations")
    public Object allocations() {
        return hostelService.allocations();
    }

    @PostMapping("/book")
    public HostelBooking book(@Valid @RequestBody HostelBookingRequest request) {
        return hostelService.book(request);
    }

    @GetMapping("/{regNo}")
    public HostelBooking allocation(@PathVariable String regNo) {
        return hostelService.allocation(regNo);
    }

    @GetMapping("/receipt/{regNo}")
    public ResponseEntity<byte[]> receipt(@PathVariable String regNo) {
        HostelBooking booking = hostelService.allocation(regNo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hostel-receipt-" + regNo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfGenerator.hostelReceipt(booking));
    }
}
