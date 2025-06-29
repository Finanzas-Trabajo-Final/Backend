package com.grupo4.americash.dto;

import java.util.List;

public record BondResponse(
        List<PaymentScheduleEntry> schedule,
        FinancialIndicators indicators
) {
}
