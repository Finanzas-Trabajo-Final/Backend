package com.grupo4.americash.dto;

import java.math.BigDecimal;

public record FinancialIndicatorsDto(
        // Estructuración del bono
            int paymentFrequencyDays,
            int capitalizationDays,
            int periodsPerYear,
            int totalPeriods,
            BigDecimal annualEffectiveRate,
            BigDecimal periodicEffectiveRate,
            BigDecimal periodicDiscountRate,
            BigDecimal issuerInitialCosts,
            BigDecimal bondholderInitialCosts,

        // Precio actual y utilidad
            BigDecimal currentPrice,
            BigDecimal profitOrLoss,

        // Ratios de decisión
            BigDecimal duration,
            BigDecimal convexity,
            BigDecimal totalDurationPlusConvexity,
            BigDecimal modifiedDuration,

        // Indicadores de rentabilidad
            BigDecimal tceaIssuer,
            BigDecimal tceaIssuerWithShield,
            BigDecimal treaBondholder
) {
}
