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
        if ("EFECTIVA".equals(bond.getInterestRateType().name())) {
            BigDecimal tea = BigDecimal.ONE.add(bond.getAnnualInterestRate());
            System.out.println("TEA"+tea);
            BigDecimal exponent = BigDecimal.valueOf(bond.getPaymentFrequencyInMonths() * 30)
                    .divide(BigDecimal.valueOf(360), mc);
            BigDecimal result = power(tea, exponent).subtract(BigDecimal.ONE);
            return result ;
        } else {
            BigDecimal nominalRate = bond.getAnnualInterestRate(); // 12% → 0.12
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

    private BigDecimal power(BigDecimal base, BigDecimal exponent) {
        double baseDouble = base.doubleValue();
        double exponentDouble = exponent.doubleValue();
        double result = Math.pow(baseDouble, exponentDouble);
        return BigDecimal.valueOf(result).round(mc);
    }

    private BigDecimal calculateInflationAdjustment(Bond bond) {
        BigDecimal inflationRate = BigDecimal.valueOf(0.10); // 10% anual

        // Días del periodo según frecuencia de pago
        int paymentPeriodInDays = bond.getPaymentFrequencyInMonths() * 30;

        // Exponente fraccional: días del período / 360
        BigDecimal exponent = BigDecimal.valueOf(paymentPeriodInDays).divide(BigDecimal.valueOf(360), mc);

        // (1 + tasa)^exponente - 1
        BigDecimal base = BigDecimal.ONE.add(inflationRate);
        BigDecimal adjustmentFactor = power(base, exponent);  // usa métod o pow con decimales
        BigDecimal inflationAdjustment = adjustmentFactor.subtract(BigDecimal.ONE);

        return inflationAdjustment;
    }

    private PaymentSchedule createPaymentSchedule(int period, BigDecimal bondValue, BigDecimal quota, BigDecimal amortization, BigDecimal inflationAdjustment, Bond bond) {
        BigDecimal adjustment =BigDecimal.ONE.add(inflationAdjustment); // Ajuste de inflación + 1
        PaymentSchedule ps = new PaymentSchedule();
        ps.setPeriod(period);
        ps.setCoupon(BigDecimal.ZERO);
        ps.setAmortization(amortization);
        ps.setQuota(quota);
        ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(0.10));
        ps.setScheduledDateInflationPeriod(inflationAdjustment.multiply(adjustment));
        ps.setGraceType("Ninguno");
        ps.setBondValue(bondValue);
        ps.setIndexedBondValue(ps.getBondValue().multiply(inflationAdjustment)); // assumed 10%
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

//TODO
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
            BigDecimal tep = calculateTXP(bond);

            BigDecimal discountRate = bond.getDiscountRate();
            int termInDays = bond.getTermInMonths() * 30;
            BigDecimal daysDifferenceMagnitude = BigDecimal.valueOf(termInDays).divide(BigDecimal.valueOf(360), mc);
            BigDecimal COK = power(BigDecimal.ONE.add(discountRate), daysDifferenceMagnitude).subtract(BigDecimal.ONE);
            System.out.println("COK: "+COK);

            PaymentSchedule ps = new PaymentSchedule();
            ps.setPeriod(i);
            ps.setScheduledDateInflationPeriod(rate);

            ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(1.1));
            ps.setGraceType("Ninguno");

          if (i == 1) {
              ps.setBondValue(bond.getFaceValue());
          } else if (!schedule.isEmpty()) {
              ps.setBondValue(schedule.get(i - 2).getIndexedBondValue());
          }
            ps.setIndexedBondValue(ps.getBondValue().multiply(BigDecimal.ONE.add(rate)));
            ps.setCoupon(ps.getIndexedBondValue().negate().multiply(tep));
            ps.setQuota(ps.getCoupon());

            ps.setTaxShield(bond.getIncomeTaxRate().negate().multiply(ps.getCoupon()).setScale(4, RoundingMode.HALF_UP));
            //TODO: cambiar el flujo para seguir esta operacin =SI(A28<=L$7,I28+K28,0)
            ps.setIssuerFlow(total.negate().setScale(6,RoundingMode.HALF_UP));

            ps.setIssuerFlowWithShield(ps.getIssuerFlow().add(ps.getTaxShield()).setScale(6, RoundingMode.HALF_UP));
            ps.setBondholderFlow(ps.getIssuerFlow().negate());
            ps.setDiscountedFlow(power(ps.getBondholderFlow(), BigDecimal.ONE.add(COK).multiply(BigDecimal.valueOf(i))));
            System.out.println("FLUJO DEL EMISOR: " + ps.getIssuerFlow());
            System.out.println("FLUJO DESCONTADO"+ps.getDiscountedFlow());
            ps.setFlowByTerm(ps.getBondholderFlow().multiply(BigDecimal.valueOf(i)).multiply(daysDifferenceMagnitude));
            ps.setConvexityFactor(ps.getDiscountedFlow().multiply(BigDecimal.valueOf(i)).multiply(BigDecimal.valueOf(i+1), mc));
            System.out.println("Convexity "+ps.getConvexityFactor());


            ps.setBond(bond);

            //VALORES QUE SON 0 HASTA EL ULTIMO PERIODO
            ps.setPremium(i == periods ? bond.getPremiumPercentage().multiply(bond.getFaceValue()) : BigDecimal.ZERO);
            ps.setAmortization(i == periods ? remaining.setScale(6, RoundingMode.HALF_UP) : amortization.setScale(6, RoundingMode.HALF_UP));
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
            System.out.println("TCEA"+tcea.setScale(2, RoundingMode.HALF_UP));
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


    @Override
    public FinancialIndicatorsDto getFinancialIndicators(Bond bond) {
        // Ejemplo con valores ya calculados.
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
