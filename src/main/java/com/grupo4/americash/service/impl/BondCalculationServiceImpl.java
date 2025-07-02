package com.grupo4.americash.service.impl;

import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.GracePeriodService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class BondCalculationServiceImpl implements BondCalculationService {
    private final GracePeriodService gracePeriodService;

    private final MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

    @Override
    public List<PaymentSchedule> generateSchedule(Bond bond) {
        List<PaymentSchedule> schedule = new ArrayList<>();

        BigDecimal interestRate = bond.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(12), mc);
        BigDecimal remaining = bond.getFaceValue();
        int periods = bond.getTermInMonths() / bond.getPaymentFrequencyInMonths();

        for (int i = 1; i <= periods; i++) {
            BigDecimal interest = remaining.multiply(interestRate, mc);
            BigDecimal amortization = bond.getFaceValue().divide(BigDecimal.valueOf(periods), mc);
            BigDecimal total = interest.add(amortization, mc);

            schedule.add(new PaymentSchedule(i, interest, amortization, total));
        }

        return gracePeriodService.applyGracePeriods(bond, schedule);
    }

    @Override
    public BigDecimal calculateTCEA(Bond bond) {
        List<PaymentSchedule> schedule = generateSchedule(bond);
        BigDecimal VCI = bond.getCommercialValue(); // Valor Comercial de Ingreso
        int n = schedule.size();
        BigDecimal sum = BigDecimal.ZERO;

        for (PaymentSchedule p : schedule) {
            int t = p.getPeriod();
            BigDecimal denom = BigDecimal.ONE.add(BigDecimal.valueOf(0.01)).pow(t, mc); // suposición r=0.01
            sum = sum.add(p.getTotal().divide(denom, mc), mc);
        }

        BigDecimal tcea = (schedule.get(n - 1).getTotal().divide(VCI, mc)).pow(12 / bond.getPaymentFrequencyInMonths(), mc).subtract(BigDecimal.ONE);
        return tcea.setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateTREA(Bond bond) {
        BigDecimal TREA = calculateTCEA(bond); // En muchos casos, TREA ≈ TCEA sin costos
        return TREA;
    }

    @Override
    public BigDecimal calculateDuration(Bond bond) {
        List<PaymentSchedule> schedule = generateSchedule(bond);
        BigDecimal rate = bond.getAnnualInterestRate().divide(BigDecimal.valueOf(12), mc);
        BigDecimal duration = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;

        for (PaymentSchedule p : schedule) {
            int t = p.getPeriod();
            BigDecimal discount = BigDecimal.ONE.add(rate).pow(t, mc);
            BigDecimal pv = p.getTotal().divide(discount, mc);

            duration = duration.add(BigDecimal.valueOf(t).multiply(pv, mc), mc);
            denominator = denominator.add(pv, mc);
        }

        return duration.divide(denominator, mc).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateModifiedDuration(Bond bond) {
        BigDecimal duration = calculateDuration(bond);
        BigDecimal rate = bond.getAnnualInterestRate().divide(BigDecimal.valueOf(12), mc);
        return duration.divide(BigDecimal.ONE.add(rate), mc).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateConvexity(Bond bond) {
        List<PaymentSchedule> schedule = new ArrayList<>();
        BigDecimal interestRate = bond.getAnnualInterestRate()
                .divide(BigDecimal.valueOf(12), mc);
        BigDecimal faceValue = bond.getFaceValue();
        int periods = bond.getTermInMonths() / bond.getPaymentFrequencyInMonths();

        for (int i = 1; i <= periods; i++) {
            BigDecimal interest = faceValue.multiply(interestRate, mc);
            BigDecimal amortization = (i == periods) ? faceValue : BigDecimal.ZERO;
            BigDecimal total = interest.add(amortization, mc);
            schedule.add(new PaymentSchedule(i, interest, amortization, total));
        }

        // Aplicar gracia
        schedule = gracePeriodService.applyGracePeriods(bond, schedule);

        // Calcular convexidad
        BigDecimal convexity = BigDecimal.ZERO;
        BigDecimal denominator = BigDecimal.ZERO;

        for (PaymentSchedule p : schedule) {
            int t = p.getPeriod();
            BigDecimal discount = BigDecimal.ONE.add(interestRate).pow(t + 2, mc);
            BigDecimal numerator = p.getTotal()
                    .multiply(BigDecimal.valueOf(t).multiply(BigDecimal.valueOf(t + 1)), mc);
            convexity = convexity.add(numerator.divide(discount, mc), mc);

            BigDecimal pv = p.getTotal().divide(BigDecimal.ONE.add(interestRate).pow(t, mc), mc);
            denominator = denominator.add(pv, mc);
        }

        BigDecimal factor = BigDecimal.valueOf(Math.pow(1 + interestRate.doubleValue(), 2));
        return convexity.divide(denominator.multiply(factor, mc), mc)
                .setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateMaxMarketPrice(Bond bond) {
        List<PaymentSchedule> schedule = generateSchedule(bond);
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal rate = bond.getAnnualInterestRate().divide(BigDecimal.valueOf(12), mc);

        for (PaymentSchedule p : schedule) {
            BigDecimal discount = BigDecimal.ONE.add(rate).pow(p.getPeriod(), mc);
            price = price.add(p.getTotal().divide(discount, mc), mc);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
