package com.grupo4.americash.service.impl;

import com.grupo4.americash.dto.FinancialIndicatorsDto;
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

    // Configuración de precisión y redondeo para cálculos financieros
    private final MathContext mc = new MathContext(18, RoundingMode.HALF_UP);


           // OPERACIONES REUTILIZABLES

    // Calcular TEP (Tasa Efectiva Periódica) o TNP (Tasa Nominal Periódica)
    private BigDecimal calculateTXP(Bond bond) {
        if ("EFFECTIVE".equals(bond.getInterestRateType().name())) {
            BigDecimal tea = bond.getAnnualInterestRate().divide(BigDecimal.valueOf(100), mc); // 12% → 0.12
            int exponent = 360 / (bond.getPaymentFrequencyInMonths() * 30); // Ensure integer exponent
            return BigDecimal.ONE.add(tea).pow(exponent, mc);
        }else {

            BigDecimal nominalRate = bond.getAnnualInterestRate().divide(BigDecimal.valueOf(100), mc); // 12% → 0.12
            BigDecimal frequency = BigDecimal.valueOf(360).divide(BigDecimal.valueOf(bond.getPaymentFrequencyInMonths() * 30), mc);
            return nominalRate.multiply(frequency, mc).add(BigDecimal.ONE);
        }

    }
    private BigDecimal calculateVAN(Bond bond) {
        List<PaymentSchedule> schedule = generateSchedule(bond);
        BigDecimal tasaDescuento = bond.getDiscountRate(); // tasa en decimal (ej: 0.2 para 20%)
        BigDecimal inversionInicial = bond.getCommercialValue(); // valor comercial como inversión inicial
        BigDecimal van = BigDecimal.ZERO;

        for (PaymentSchedule p : schedule) {
            int t = p.getPeriod();
            BigDecimal flujo = p.getIssuerFlow(); // flujo del emisor (negativo)
            BigDecimal divisor = BigDecimal.ONE.add(tasaDescuento).pow(t, mc);
            BigDecimal valorPresente = flujo.divide(divisor, mc);
            van = van.add(valorPresente, mc);
        }

        return van.subtract(inversionInicial).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTIR(Bond bond) {
        // Implementación de TIR (Tasa Interna de Retorno)
        return BigDecimal.ZERO; // Placeholder
    }



    private BigDecimal calculateInflationAdjustment(Bond bond) {
        // Asumimos una inflación anual del 10% para el ajuste
        BigDecimal inflationRate = BigDecimal.valueOf(0.10);
        BigDecimal inflationAdjustment = BigDecimal.ONE.add(inflationRate).pow(360 / (bond.getPaymentFrequencyInMonths() * 30), mc); // Ajuste por inflación anualizado
        BigDecimal finalInflationRate = inflationAdjustment.subtract(BigDecimal.ONE);

        return finalInflationRate; // Ajuste final

    }
    private PaymentSchedule createPaymentSchedule(int period, BigDecimal bondValue, BigDecimal quota, BigDecimal amortization, BigDecimal inflationAdjustment, Bond bond) {
        PaymentSchedule ps = new PaymentSchedule();
        ps.setPeriod(period);
        ps.setCoupon(BigDecimal.ZERO);
        ps.setAmortization(amortization);
        ps.setQuota(quota);
        ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(1.10));
        ps.setScheduledDateInflationPeriod(inflationAdjustment);
        ps.setGraceType("Ninguno");
        ps.setBondValue(bondValue);
        ps.setIndexedBondValue(bondValue.multiply(BigDecimal.valueOf(1.1))); // assumed 10%
        ps.setPremium(BigDecimal.ZERO);
        ps.setTaxShield(BigDecimal.ZERO);
        ps.setIssuerFlow(quota.negate().setScale(6, RoundingMode.HALF_UP));
        ps.setIssuerFlowWithShield(BigDecimal.ZERO);
        ps.setBondholderFlow(BigDecimal.ONE);
        ps.setDiscountedFlow(BigDecimal.ZERO);
        ps.setFlowByTerm(BigDecimal.ZERO);
        ps.setConvexityFactor(BigDecimal.ZERO);
        ps.setBond(bond);
        return ps;
    }


    @Override
    public List<PaymentSchedule> generateSchedule(Bond bond) {
        List<PaymentSchedule> schedule = new ArrayList<>();

        BigDecimal remaining = bond.getFaceValue();
        int periods = bond.getTermInMonths() / bond.getPaymentFrequencyInMonths();

        for (int i = 1; i <= periods; i++) {
            BigDecimal interest = remaining.multiply(calculateTXP(bond), mc);
            BigDecimal amortization = bond.getFaceValue().divide(BigDecimal.valueOf(periods), mc);
            BigDecimal total = interest.add(amortization, mc);
            BigDecimal rate = calculateInflationAdjustment(bond);
            System.out.println("Bondholder Flow: " + total.setScale(6, RoundingMode.HALF_UP));
            System.out.println("Interest: " + interest);
            System.out.println("Amortization: " + amortization);
            System.out.println("Total (cuota): " + total);
            System.out.println("Rate: " + rate);
            PaymentSchedule ps = new PaymentSchedule();
            ps.setPeriod(i);
            ps.setScheduledDateInflationPeriod(rate);
            ps.setCoupon(BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP));
            ps.setAmortization(amortization.setScale(6, RoundingMode.HALF_UP));
            ps.setQuota(total.setScale(6, RoundingMode.HALF_UP));
            ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(1.1));
            // Inicializamos el tipo de gracia como "Ninguno", luego la funcion applyGracePeriods lo modificará si es necesario
            ps.setGraceType("Ninguno");
            ps.setBondValue(bond.getFaceValue());
            ps.setIndexedBondValue(bond.getFaceValue().multiply(BigDecimal.valueOf(1.1))); // asumimos 10%
            ps.setPremium(BigDecimal.ZERO);
            ps.setTaxShield(BigDecimal.ZERO);
            ps.setIssuerFlow(total.negate().setScale(6,RoundingMode.HALF_UP)); // o lo que defina tu lógica
            ps.setIssuerFlowWithShield(BigDecimal.ZERO);
            ps.setBondholderFlow(total.setScale(6, RoundingMode.HALF_UP)); // <-- mejor que usar BigDecimal.ONE
            ps.setDiscountedFlow(BigDecimal.ZERO);
            ps.setFlowByTerm(BigDecimal.ZERO);
            ps.setConvexityFactor(BigDecimal.ZERO);
            ps.setBond(bond);

            schedule.add(ps);
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
                sum = sum.add(p.getIssuerFlow().divide(denom, mc), mc).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal tcea = (schedule.get(n - 1).getQuota().divide(VCI, mc)).pow(12 / bond.getPaymentFrequencyInMonths(), mc).subtract(BigDecimal.ONE);
            System.out.println(tcea.setScale(2, RoundingMode.HALF_UP));
            return BigDecimal.ZERO;
        }

    @Override
    public BigDecimal calculateTREA(Bond bond) {
        BigDecimal TREA = calculateTCEA(bond); // En muchos casos, TREA ≈ TCEA sin costos
        return BigDecimal.ZERO;
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
            BigDecimal pv = p.getQuota().divide(discount, mc);

            duration = duration.add(BigDecimal.valueOf(t).multiply(pv, mc), mc);
            denominator = denominator.add(pv, mc);
        }
        System.out.println("Duration: " + duration);
        System.out.println("Denominator: " + denominator);
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Evitar división por cero
        } else if (duration.compareTo(BigDecimal.ZERO)==0) {
            return BigDecimal.ZERO;
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
            PaymentSchedule ps = createPaymentSchedule(i, bond.getFaceValue(), total, amortization, calculateInflationAdjustment(bond), bond);
            schedule.add(ps);
        }

        // Aplicar gracia
        schedule = gracePeriodService.applyGracePeriods(bond, schedule);

        // Calcular convexidad
        BigDecimal convexity = BigDecimal.valueOf(100); //inicializamos variable en 0

        BigDecimal denominator =BigDecimal.valueOf(10);


        for (PaymentSchedule p : schedule) {
            int t = p.getPeriod();
            BigDecimal discount = BigDecimal.ONE.add(interestRate).pow(t + 2, mc);
            BigDecimal numerator = p.getQuota()
                    .multiply(BigDecimal.valueOf(t).multiply(BigDecimal.valueOf(t + 1)), mc);
            convexity = convexity.add(numerator.divide(discount, mc), mc);

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
            price = price.add(p.getQuota().divide(discount, mc), mc);
        }

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @param bond
     * @return
     */
    @Override
    public FinancialIndicatorsDto getFinancialIndicators(Bond bond) {
        // Ejemplo con valores ya calculados. Debes completar con lógica real
        return new FinancialIndicatorsDto(
                180,
                60,
                2,
                10,
                BigDecimal.valueOf(0.08),
                BigDecimal.valueOf(0.03923),
                BigDecimal.valueOf(0.02225),
                BigDecimal.valueOf(2.16),
                BigDecimal.valueOf(0.93),
                BigDecimal.valueOf(175.33),
                BigDecimal.valueOf(76.40),
                BigDecimal.valueOf(4.45),
                BigDecimal.valueOf(22.39),
                BigDecimal.valueOf(26.84),
                BigDecimal.valueOf(4.35),
                BigDecimal.valueOf(0.2011995),
                BigDecimal.valueOf(0.1736642),
                BigDecimal.valueOf(0.1922097)
        );
    }

}
