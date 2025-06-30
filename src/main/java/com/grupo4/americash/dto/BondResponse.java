package com.grupo4.americash.dto;

import com.grupo4.americash.entity.PaymentSchedule;

import java.util.List;

public record BondResponse(
        List<PaymentSchedule> schedule,
        FinancialIndicators indicators
) {
}
