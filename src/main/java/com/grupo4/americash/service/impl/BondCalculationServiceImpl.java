package com.grupo4.americash.service.impl;

import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.GracePeriodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class BondCalculationServiceImpl implements BondCalculationService {
    private final GracePeriodService gracePeriodService;

    @Override
    public List<PaymentSchedule> generateSchedule(Bond bond) {
        return List.of();
    }

    @Override
    public BigDecimal calculateTCEA(Bond bond) {
        return null;
    }

    @Override
    public BigDecimal calculateTREA(Bond bond) {
        return null;
    }

    @Override
    public BigDecimal calculateDuration(Bond bond) {
        return null;
    }

    @Override
    public BigDecimal calculateModifiedDuration(Bond bond) {
        return null;
    }

    @Override
    public BigDecimal calculateConvexity(Bond bond) {
        return null;
    }

    @Override
    public BigDecimal calculateMaxMarketPrice(Bond bond) {
        return null;
    }
}
