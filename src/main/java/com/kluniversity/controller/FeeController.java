package com.kluniversity.controller;

import com.kluniversity.dto.FeePaymentRequest;
import com.kluniversity.entity.Payment;
import com.kluniversity.repository.PaymentRepository;
import com.kluniversity.service.FeeService;
import com.kluniversity.util.PdfGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;
    private final PaymentRepository paymentRepository;
    private final PdfGenerator pdfGenerator;

    @PostMapping("/pay")
    public Payment pay(@Valid @RequestBody FeePaymentRequest request) {
        return feeService.pay(request);
    }

    @GetMapping("/history/{regNo}")
    public Object history(@PathVariable String regNo) {
        return feeService.feeHistory(regNo);
    }

    @GetMapping("/payments/{regNo}")
    public Object payments(@PathVariable String regNo) {
        return feeService.paymentHistory(regNo);
    }

    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<byte[]> receipt(@PathVariable Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fee-receipt-" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfGenerator.feeReceipt(payment));
    }
}
