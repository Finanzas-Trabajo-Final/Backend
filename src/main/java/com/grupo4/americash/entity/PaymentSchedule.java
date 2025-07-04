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

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal scheduledDateInflationAnnual;     // Inflación Anual

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal scheduledDateInflationPeriod;     // Inflación Semestral o de periodo

    @Column(nullable = false)
    private String graceType;                            // Plazo de Gracia ("Total", "Parcial", o "Ninguno")

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal bondValue;                        // Bono

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal indexedBondValue;                 // Bono Indexado

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal coupon;                           // Cupon (Interés)

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal quota;                            // Cuota total

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal amortization;                     // Amortización

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal premium;                          // Prima

    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal taxShield;                        // Escudo
    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal issuerFlow;                       // Flujo Emisor
    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal issuerFlowWithShield;             // Flujo Emisor c/Escudo
    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal bondholderFlow;                   // Flujo Bonista
    @Column(nullable = false,precision = 18, scale =6)

    private BigDecimal discountedFlow;                   // Flujo Actualizado
    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal flowByTerm;                       // FA x Plazo
    @Column(nullable = false,precision = 18, scale =6)
    private BigDecimal convexityFactor;                  // Factor para Convexidad

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bond_id")
    private Bond bond;
}
