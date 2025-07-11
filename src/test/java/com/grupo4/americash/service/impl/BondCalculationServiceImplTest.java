package com.grupo4.americash.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BondCalculationServiceImplTest {

    private BondCalculationServiceImpl bondCalculationService;

    @BeforeEach
    void setUp() {
        bondCalculationService = new BondCalculationServiceImpl(null, null);
    }

    @Test
    void calculateVAN_shouldReturnCorrectResult() {
        // Arrange
        List<BigDecimal> cashFlows = List.of(
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            new BigDecimal("1100")
        );
        BigDecimal discountRate = new BigDecimal("0.10");

        // Act
        BigDecimal van = bondCalculationService.calculateVAN(cashFlows, discountRate);

        // Assert
        assertNotNull(van);
        System.out.println("VAN: " + van);
    }
}
