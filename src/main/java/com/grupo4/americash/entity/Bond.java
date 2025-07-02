package com.grupo4.americash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        @Column(nullable = false)
        private int totalGraceMonths;

        @Column(nullable = false)
        private int partialGraceMonths;

        @Column(precision = 5, scale = 4)
        private BigDecimal discountRate;

        @Column(precision = 5, scale = 4)
        private BigDecimal incomeTaxRate;

        @Column(precision = 5, scale = 4)
        private BigDecimal premiumPercentage;

        @Column(precision = 5, scale = 4)
        private BigDecimal structuringCostPercentage;

        @Column(precision = 5, scale = 4)
        private BigDecimal placementCostPercentage;

        @Column(precision = 5, scale = 4)
        private BigDecimal flotationCostPercentage;

        @Column(precision = 5, scale = 4)
        private BigDecimal cavaliCostPercentage;

        @Column(nullable = false)
        private LocalDate disbursementDate;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id") // optional: track who created it
        private User user;

        @OneToMany(mappedBy = "bond", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<PaymentSchedule> schedule;



        @Column(precision = 12, scale = 7)
        private BigDecimal modifiedDuration;

        @Column(precision = 12, scale = 7)
        private BigDecimal tcea;

        @Column(precision = 12, scale = 7)
        private BigDecimal trea;

        @Column(precision = 12, scale = 7)
        private BigDecimal duration;

        @Column(precision = 12, scale = 7)
        private BigDecimal convexity;


        public void setSchedule(List<PaymentSchedule> schedule) {
            this.schedule = schedule;
            for (PaymentSchedule p : schedule) {
                p.setBond(this);
            }
    }
    }