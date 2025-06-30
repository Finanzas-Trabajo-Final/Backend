package com.grupo4.americash.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentSchedule {
    private int period;
    private BigDecimal interest;
    private BigDecimal amortization;
    private BigDecimal total;
}
