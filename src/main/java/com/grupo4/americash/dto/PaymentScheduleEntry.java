package com.grupo4.americash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentScheduleEntry(
        int period,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "Payment date", example = "2025-08-01")
        LocalDate paymentDate,

        @Schema(description = "Interest paid this period", example = "100.00")
        BigDecimal interest,

        @Schema(description = "Amortization paid this period", example = "0.00")
        BigDecimal amortization,

        @Schema(description = "Total payment this period", example = "100.00")
        BigDecimal totalPayment,

        @Schema(description = "Remaining principal balance", example = "10000.00")
        BigDecimal remainingBalance,

        @Schema(description = "Type of grace: none, partial, total", example = "partial")
        String graceType
) {
}
