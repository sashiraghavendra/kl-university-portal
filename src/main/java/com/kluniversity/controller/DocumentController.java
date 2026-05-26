package com.kluniversity.controller;

import com.kluniversity.entity.HostelBooking;
import com.kluniversity.service.HostelService;
import com.kluniversity.service.StudentService;
import com.kluniversity.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final StudentService studentService;
    private final HostelService hostelService;
    private final PdfGenerator pdfGenerator;

    @GetMapping("/id-card/{regNo}")
    public ResponseEntity<byte[]> idCard(@PathVariable String regNo) {
        HostelBooking booking = null;
        try {
            booking = hostelService.allocation(regNo);
        } catch (RuntimeException ignored) {
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=id-card-" + regNo + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfGenerator.idCard(studentService.get(regNo), booking));
    }
}
