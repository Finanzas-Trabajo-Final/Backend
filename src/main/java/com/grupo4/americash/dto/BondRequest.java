package com.grupo4.americash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BondRequest(

        @Schema(description = "Issuer of the bond", example = "Banco de América")
        String issuer,

        @Schema(description = "Face value of the bond", example = "100.00")
        BigDecimal faceValue,

        @Schema(description = "Commercial value (price) of the bond", example = "98.00")
        BigDecimal commercialValue,

        @Schema(description = "Interest rate type: 'nominal' or 'effective'", example = "EFECTIVA")
        String interestRateType,

        @Schema(description = "Annual interest rate (decimal)", example = "0.08")
        BigDecimal annualInterestRate,

        @Schema(description = "Capitalization period in days", example = "60")
        Integer capitalizationPeriod,

        @Schema(description = "Bond term in months", example = "5")
        int termInYears,

        @Schema(description = "Payment frequency in months", example = "6")
        int paymentFrequencyInMonths,

        @Schema(description = "Number of months with total grace", example = "0")
        int totalGraceMonths,

        @Schema(description = "Number of months with partial grace", example = "0")
        int partialGraceMonths,

        @Schema(description = "Currency of the bond", example = "PEN")
        String currency,

        @Schema(description = "Discount rate (decimal)", example = "0.045")
        BigDecimal discountRate,

        @Schema(description = "Income tax rate (decimal)", example = "0.30")
        BigDecimal incomeTaxRate,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @Schema(description = "Disbursement date", example = "2025-06-01")
        LocalDate disbursementDate,

        @Schema(description = "Premium percentage (decimal)", example = "0.01")
        BigDecimal premiumPercentage,

        @Schema(description = "Structuring cost percentage (decimal)", example = "0.01")
        BigDecimal structuringCostPercentage,

        @Schema(description = "Placement cost percentage (decimal)", example = "0.0025")
        BigDecimal placementCostPercentage,

        @Schema(description = "Flotation cost percentage (decimal)", example = "0.0045")
        BigDecimal flotationCostPercentage,

        @Schema(description = "Cavali cost percentage (decimal)", example = "0.005")
        BigDecimal cavaliCostPercentage

) {
}