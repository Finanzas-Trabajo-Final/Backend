package com.grupo4.americash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BondRequest(
        @Schema(description = "Face value of the bond", example = "10000.00")
        BigDecimal faceValue,

        @Schema(description = "Issuer of the bond", example = "Banco de América")
        String issuer,

        @Schema(description = "Commercial value (price) of the bond", example = "9800.00")
        BigDecimal commercialValue,

        @Schema(description = "Interest rate type: 'nominal' or 'effective'", example = "nominal")
        String interestRateType,

        @Schema(description = "Annual interest rate as a percentage", example = "12.0")
        BigDecimal annualInterestRate,

        @Schema(description = "Capitalization period (only for nominal rate)", example = "2")
        Integer capitalizationPeriod,

        @Schema(description = "Bond term in months", example = "12")
        int termInMonths,

        @Schema(description = "Payment frequency in months", example = "1")
        int paymentFrequencyInMonths,

        @Schema(description = "Number of months with total grace", example = "1")
        int totalGraceMonths,

        @Schema(description = "Number of months with partial grace", example = "1")
        int partialGraceMonths,

        @Schema(description = "Currency of the bond", example = "PEN")
        String currency,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "Disbursement date", example = "2025-07-01")
        LocalDate disbursementDate,

         @Schema(description = "ID of the user", example = "1")
        Long userId
) {
}
