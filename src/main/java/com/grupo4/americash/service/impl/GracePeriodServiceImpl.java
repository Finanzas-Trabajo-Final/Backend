package com.grupo4.americash.service.impl;

import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.service.GracePeriodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class GracePeriodServiceImpl implements GracePeriodService {

    @Override
    public List<PaymentSchedule> applyGracePeriods(Bond bond, List<PaymentSchedule> schedule) {
        int totalGrace = bond.getTotalGraceMonths() / bond.getPaymentFrequencyInMonths();
        int partialGrace = bond.getPartialGraceMonths() / bond.getPaymentFrequencyInMonths();

        for (int i = 0; i < schedule.size(); i++) {
            PaymentSchedule p = schedule.get(i);
            if (i < totalGrace) {
                p.setQuota(BigDecimal.ZERO);
                p.setAmortization(BigDecimal.ZERO);
                p.setPremium(BigDecimal.ZERO);
                p.setGraceType("Total");
            } else if (i < totalGrace + partialGrace) {
                p.setAmortization(BigDecimal.ZERO);
                p.setGraceType("Parcial");
            } else {
                p.setGraceType("Ninguno");
            }
        }

        return schedule;
    }

}
