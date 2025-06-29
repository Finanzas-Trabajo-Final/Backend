package com.grupo4.americash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bonds")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Bond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String issuer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false)
    private BigDecimal faceValue;

    @Column(nullable = false)
    private BigDecimal commercialValue;

    @Column(nullable = false)
    private String interestRateType; // "nominal" or "effective"

    @Column(nullable = false)
    private BigDecimal annualInterestRate;

    private Integer capitalizationPeriod; // nullable if rate is effective

    @Column(nullable = false)
    private int termInMonths;

    @Column(nullable = false)
    private int paymentFrequencyInMonths;

    private int totalGraceMonths;
    private int partialGraceMonths;

    @Column(nullable = false)
    private LocalDate disbursementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // optional: track who created it
    private User user;
}
