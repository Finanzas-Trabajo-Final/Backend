package com.grupo4.americash.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.grupo4.americash.entity.Bond;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BondDto(

        Long id,

        @NotBlank
        String issuer,
        // DATOS DE ENTRADA

        @NotBlank
        String currency,

        @NotNull
        BigDecimal faceValue,

        @NotNull
        BigDecimal commercialValue,

        @NotBlank
        String interestRateType, // "nominal" or "effective"

        @NotNull
        BigDecimal annualInterestRate,

        Integer capitalizationPeriod,

        @NotNull
        Integer termInMonths,

        @NotNull
        Integer paymentFrequencyInMonths,

        @NotNull
        Integer totalGraceMonths,

        @NotNull
        Integer partialGraceMonths,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate disbursementDate,

        @NotNull
        BigDecimal premiumPercentage,

        @NotNull
        BigDecimal structuringCostPercentage,
        //COSTOS INICIALES
        @NotNull
        BigDecimal placementCostPercentage,

        @NotNull
        BigDecimal flotationCostPercentage,

        @NotNull
        BigDecimal cavaliCostPercentage,

        String user

) {

    public BondDto(Bond bond) {
        this(
                bond.getId(),
                bond.getIssuer(),
                bond.getCurrency().name(),
                bond.getFaceValue(),
                bond.getCommercialValue(),
                bond.getInterestRateType().name(),
                bond.getAnnualInterestRate(),
                bond.getCapitalizationPeriod(),
                bond.getTermInMonths(),
                bond.getPaymentFrequencyInMonths(),
                bond.getTotalGraceMonths(),
                bond.getPartialGraceMonths(),
                bond.getDisbursementDate(),
                bond.getPremiumPercentage(),
                bond.getStructuringCostPercentage(),
                bond.getPlacementCostPercentage(),
                bond.getFlotationCostPercentage(),
                bond.getCavaliCostPercentage(),
                bond.getUser().getUsername()

        );
    }

}