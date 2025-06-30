package com.grupo4.americash.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int period;
    private BigDecimal interest;
    private BigDecimal amortization;
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;

    public PaymentSchedule(int period, BigDecimal interest, BigDecimal amortization, BigDecimal total) {
        this.period = period;
        this.interest = interest;
        this.amortization = amortization;
        this.total = total;
    }
}
