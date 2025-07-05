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

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal scheduledDateInflationAnnual;     // Inflación Anual

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal scheduledDateInflationPeriod;     // Inflación del período

    @Column(nullable = false)
    private String graceType;                            // Tipo de gracia

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal bondValue;                        // Valor nominal del bono

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal indexedBondValue;                 // Bono indexado por inflación

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal coupon;                           // Interés del periodo

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quota;                            // Cuota total (interés + amortización)

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal amortization;                     // Amortización del capital

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal premium;                          // Prima (último periodo)

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal taxShield;                        // Escudo fiscal

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal issuerFlow;                       // Flujo del emisor

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal issuerFlowWithShield;             // Flujo del emisor con escudo

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal bondholderFlow;                   // Flujo del bonista

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal discountedFlow;                   // Flujo descontado

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal flowByTerm;                       // Flujo por plazo

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal convexityFactor;                  // Factor para convexidad

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;
}