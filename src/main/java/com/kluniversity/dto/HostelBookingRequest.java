package com.kluniversity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HostelBookingRequest {
    @NotBlank
    private String regNo;
    @NotNull
    private Long roomId;
    @NotBlank
    private String messType;
}
