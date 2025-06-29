package com.grupo4.americash.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record FinancialIndicators(
        @Schema(description = "TCEA - Effective cost rate for the issuer", example = "13.50")
        BigDecimal tcea,

        @Schema(description = "TREA - Effective yield rate for the investor", example = "12.60")
        BigDecimal trea,

        @Schema(description = "Duration of the bond", example = "10.45")
        BigDecimal duration,

        @Schema(description = "Modified duration", example = "9.95")
        BigDecimal modifiedDuration,

        @Schema(description = "Convexity of the bond", example = "111.84")
        BigDecimal convexity,

        @Schema(description = "Maximum market price", example = "10150.00")
        BigDecimal maxMarketPrice
) {
}
