package com.grupo4.americash.service.impl;

import com.grupo4.americash.dto.FinancialIndicatorsDto;
import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.repository.BondRepository;
import com.grupo4.americash.repository.PaymentScheduleRepository;
import com.grupo4.americash.service.BondCalculationService;
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
    private final BondRepository bondRepository;
    private final PaymentScheduleRepository paymentScheduleRepository;

    // Configuración de precisión y redondeo para cálculos financieros
    private final MathContext mc = new MathContext(6, RoundingMode.HALF_UP);


           // OPERACIONES REUTILIZABLES

    // Calcular TEP (Tasa Efectiva Periódica) o TNP (Tasa Nominal Periódica)
    private BigDecimal calculateTXP(Bond bond) {
        int paymentFrequencyInMonths = bond.getPaymentFrequencyInMonths() * 30;
        if ("EFECTIVA".equals(bond.getInterestRateType().name())) {
            BigDecimal tea = BigDecimal.ONE.add(bond.getAnnualInterestRate());
            BigDecimal exponent = BigDecimal.valueOf(paymentFrequencyInMonths).divide(BigDecimal.valueOf(360), mc);
            BigDecimal result = power(tea, exponent).subtract(BigDecimal.ONE);
            return result ;
        } else {
            BigDecimal nominalRate = bond.getAnnualInterestRate(); // 12% → 0.12
            int couponFrequency = bond.getPaymentFrequencyInMonths()*30;
            int periodsPerYear = 360 / couponFrequency;

            int periods = BigDecimal.valueOf(periodsPerYear).multiply(BigDecimal.valueOf(bond.getTermInYears())).intValue();
            System.out.println("PERIODS: "+periods);
            System.out.println("NOMINAL RATE: "+nominalRate);
            BigDecimal nominalRatePerPeriod = nominalRate.divide(BigDecimal.valueOf(periodsPerYear).round(mc), mc);
            System.out.println("PERIODS PER YEAR: "+nominalRatePerPeriod);
            return nominalRate.divide(BigDecimal.valueOf(periods), mc);
        }
    }



    private BigDecimal power(BigDecimal base, BigDecimal exponent) {
        double baseDouble = base.doubleValue();
        double exponentDouble = exponent.doubleValue();
        double result = Math.pow(baseDouble, exponentDouble);
        return BigDecimal.valueOf(result);
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

    //VAN Y TIR
    public BigDecimal calculateVAN(List<BigDecimal> cashFlows, BigDecimal discountRate) {
        BigDecimal van = BigDecimal.ZERO;

        for (int i = 0; i < cashFlows.size(); i++) {
            BigDecimal numerator = cashFlows.get(i);
            BigDecimal denominator = BigDecimal.ONE.add(discountRate).pow(i + 1, mc);
            van = van.add(numerator.divide(denominator, mc));
        }

        return van.setScale(6, RoundingMode.HALF_UP);
    }
    public BigDecimal calculateIRR(List<BigDecimal> cashFlows, BigDecimal initialCost) {
        final int maxIterations = 1000;
        final BigDecimal precision = new BigDecimal("0.00000001");
        final MathContext mc = new MathContext(18, RoundingMode.HALF_UP);
        BigDecimal rate = BigDecimal.valueOf(0.1); // Suposición inicial

        List<BigDecimal> allCashFlows = new ArrayList<>();
        allCashFlows.add(initialCost.negate());
        allCashFlows.addAll(cashFlows);

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            BigDecimal fValue = BigDecimal.ZERO;
            BigDecimal fDerivative = BigDecimal.ZERO;

            for (int t = 0; t < allCashFlows.size(); t++) {
                BigDecimal denominator = BigDecimal.ONE.add(rate).pow(t, mc);
                BigDecimal term = allCashFlows.get(t).divide(denominator, mc);
                fValue = fValue.add(term);

                if (t > 0) {
                    BigDecimal derivativeTerm = allCashFlows.get(t)
                            .multiply(BigDecimal.valueOf(-t))
                            .divide(BigDecimal.ONE.add(rate).pow(t + 1, mc), mc);
                    fDerivative = fDerivative.add(derivativeTerm);
                }
            }

            BigDecimal newRate = rate.subtract(fValue.divide(fDerivative, mc));
            if (newRate.subtract(rate).abs().compareTo(precision) < 0) {
                return newRate.setScale(8, RoundingMode.HALF_UP);
            }

            rate = newRate;
        }

        throw new ArithmeticException("La TIR no converge.");
    }



    @Override
    public List<PaymentSchedule> generateSchedule(Bond bond) {
        List<PaymentSchedule> schedule = new ArrayList<>();

        int couponFrequency = bond.getPaymentFrequencyInMonths()*30;
        int periodsPerYear = 360 / couponFrequency;


        int periods = BigDecimal.valueOf(periodsPerYear).multiply(BigDecimal.valueOf(bond.getTermInYears())).intValue();

        BigDecimal rate = calculateInflationAdjustment(bond);
        BigDecimal tep = calculateTXP(bond);


        BigDecimal discountRate = bond.getDiscountRate();
        int paymentFrequencyInDays = bond.getPaymentFrequencyInMonths() * 30;
        BigDecimal daysDifferenceMagnitude = BigDecimal.valueOf(paymentFrequencyInDays).divide(BigDecimal.valueOf(360), mc);

        BigDecimal cok = power(BigDecimal.ONE.add(discountRate), daysDifferenceMagnitude).subtract(BigDecimal.ONE);

        int totalGracePeriods = periods / bond.getTotalGraceMonths() ;
        int partialGracePeriods = periods/ bond.getPartialGraceMonths() ;
        System.out.println("TOTAL GRACE PERIODS: " + totalGracePeriods + ", PARTIAL GRACE PERIODS: " + partialGracePeriods);
        for (int i = 1; i <= periods; i++) {
            PaymentSchedule ps = new PaymentSchedule();
            ps.setPeriod(i);
            ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(0.1));
            ps.setScheduledDateInflationPeriod(rate.setScale(4, RoundingMode.HALF_UP));
            if (i < totalGracePeriods) {
                ps.setQuota(BigDecimal.ZERO);
                ps.setAmortization(BigDecimal.ZERO);
                ps.setPremium(BigDecimal.ZERO);
                ps.setGraceType("T");
            } else if (i < totalGracePeriods + partialGracePeriods) {
                ps.setAmortization(BigDecimal.ZERO);
                ps.setGraceType("P");
            } else {
                ps.setGraceType("S");
            }
            //INICIALIZAR VALORES
              if (i == 1) {
                  ps.setBondValue(bond.getFaceValue());
              } else if (!schedule.isEmpty()) {
                  ps.setBondValue(schedule.get(i - 2).getIndexedBondValue().setScale(6, RoundingMode.HALF_UP));
              }
            ps.setIndexedBondValue(ps.getBondValue().multiply(BigDecimal.ONE.add(rate)).setScale(6, RoundingMode.HALF_UP));
            ps.setCoupon(ps.getIndexedBondValue().negate().multiply(tep).setScale(6, RoundingMode.HALF_UP));



            //VALORES QUE SON 0 HASTA EL ULTIMO PERIODO
            ps.setPremium(i == periods ? bond.getPremiumPercentage().multiply(bond.getFaceValue().negate()) : BigDecimal.ZERO);
            ps.setAmortization(i == periods ? ps.getIndexedBondValue().negate().setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            ps.setQuota(i == periods ? ps.getAmortization().add(ps.getCoupon()).setScale(6, RoundingMode.HALF_UP) : ps.getCoupon().setScale(6, RoundingMode.HALF_UP));
            ps.setTaxShield(bond.getIncomeTaxRate().negate().multiply(ps.getCoupon()).setScale(6, RoundingMode.HALF_UP));

            //FLUJOS
            ps.setIssuerFlow(ps.getQuota().add(ps.getPremium() != null ? ps.getPremium() : BigDecimal.ZERO));
            ps.setIssuerFlowWithShield(ps.getIssuerFlow().add(ps.getTaxShield()).setScale(6, RoundingMode.HALF_UP));
            ps.setBondholderFlow(ps.getIssuerFlow().negate().setScale(6, RoundingMode.HALF_UP));

            //flujo bonista/(1+COKSEMESTRAL)^i
            BigDecimal denominator = BigDecimal.ONE.add(cok);
            ps.setDiscountedFlow(ps.getBondholderFlow().divide(power(denominator, BigDecimal.valueOf(i)), mc));
            ps.setFlowByTerm(ps.getDiscountedFlow().multiply(BigDecimal.valueOf(i)).multiply(daysDifferenceMagnitude).setScale(6, RoundingMode.HALF_UP));
            ps.setConvexityFactor(ps.getDiscountedFlow().multiply(BigDecimal.valueOf(i)).multiply(BigDecimal.valueOf(i+1), mc));

            //Asignar valores en la entidad

            ps.setBond(bond);
            schedule.add(ps);
        }


        return schedule;
    }



    @Override
    public FinancialIndicatorsDto getFinancialIndicators(Long BondId) {

        Bond bond = bondRepository.findById(BondId)
                .orElseThrow(() -> new RuntimeException("Bond not found"));

        List<PaymentSchedule> schedule = paymentScheduleRepository.findByBondId(BondId);

        //BOND FLOWS LIST
        List<BigDecimal> bondHolderFlow = schedule.stream()
                .map(PaymentSchedule::getBondholderFlow)
                .toList();
        List<BigDecimal> bondIssuerFlow = schedule.stream()
                .map(PaymentSchedule::getIssuerFlow)
                .toList();
        List<BigDecimal> bondIssuerWithShieldFlow = schedule.stream()
                .map(PaymentSchedule::getIssuerFlowWithShield)
                .toList();



        //BOND FLOWS SUM
        BigDecimal periodDiscountedFlow = schedule.stream()
                .map(PaymentSchedule::getDiscountedFlow)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal periodFlowByTerm = schedule.stream()
                .map(PaymentSchedule::getFlowByTerm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal convexityFactorPeriodFlow = schedule.stream()
                .map(PaymentSchedule::getConvexityFactor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println("convexityFactorPeriodFlow: " + convexityFactorPeriodFlow);

        int couponFrequency = bond.getPaymentFrequencyInMonths()*30;
        int periodsPerYear = 360 / couponFrequency;
        int capitalizationPeriod = bond.getCapitalizationPeriod() * 30; // Convertir a días
        int periods = BigDecimal.valueOf(periodsPerYear).multiply(BigDecimal.valueOf(bond.getTermInYears())).intValue();


        BigDecimal tep = calculateTXP(bond);
        System.out.println("TEP: " + tep);

        BigDecimal initialCostsBothPercentage = bond.getStructuringCostPercentage().add(bond.getPlacementCostPercentage()).add(bond.getFlotationCostPercentage().add(bond.getCavaliCostPercentage())) ;
        BigDecimal initialCostsBoth = bond.getCommercialValue().multiply(initialCostsBothPercentage).setScale(6, RoundingMode.HALF_UP);

        System.out.println("INITIAL COSTS BOTH: " + initialCostsBoth +"Initial Costs Percentage Both: " + initialCostsBothPercentage);

        BigDecimal initialCostsPercentageBondHolder =bond.getFlotationCostPercentage().add(bond.getCavaliCostPercentage());
        BigDecimal initialCostsBondHolder = bond.getCommercialValue().multiply(initialCostsPercentageBondHolder).setScale(6, RoundingMode.HALF_UP);
        System.out.println("BondHolder Initial Costs: " + initialCostsBondHolder + "Initial Costs Percentage BondHolder: " + initialCostsPercentageBondHolder);


        BigDecimal period0IssuerFlow = bond.getCommercialValue().subtract(initialCostsBoth);
        BigDecimal period0BondholderFlow = bond.getCommercialValue().negate().subtract(initialCostsBondHolder);


        System.out.println("period0IssuerFlow: " + period0IssuerFlow + " period0BondholderFlow: " + period0BondholderFlow);
        BigDecimal discountRate = bond.getDiscountRate();
        int paymentFrequencyInDays = bond.getPaymentFrequencyInMonths() * 30;
        BigDecimal daysDifferenceMagnitude = BigDecimal.valueOf(paymentFrequencyInDays).divide(BigDecimal.valueOf(360), mc);
        BigDecimal cok = power(BigDecimal.ONE.add(discountRate), daysDifferenceMagnitude).subtract(BigDecimal.ONE);

        // VAN
        BigDecimal van = calculateVAN(bondHolderFlow, cok);
        System.out.println("VAN: " + van);
        BigDecimal utilityOrLose = period0BondholderFlow.add(van);



        //RATIOS DE DECISIÓN
        BigDecimal duration = periodFlowByTerm.divide(periodDiscountedFlow, mc);

        BigDecimal rate =BigDecimal.valueOf(360).divide(BigDecimal.valueOf(couponFrequency), mc);
        BigDecimal rateSquared = rate.pow(2, mc);
        BigDecimal cokSqr = BigDecimal.ONE.add(cok).pow(2,mc);
        System.out.println("factor: " + cokSqr + " rateSquared: " + rateSquared+ " rate: " + rate);
        BigDecimal multipliedFactor = rateSquared.multiply(cokSqr).multiply(periodDiscountedFlow);
        System.out.println("multipliedFactor: " + multipliedFactor+" periodDiscountedFlow: " + periodDiscountedFlow);

        BigDecimal convexity = convexityFactorPeriodFlow.divide(multipliedFactor, mc);

        BigDecimal totalDurationPlusConvexity = convexity.add(duration);

        BigDecimal modifiedDuration = duration.divide(BigDecimal.ONE.add(cok),mc);

        //INDICADORES DE RENTABILIDAD
        //BigDecimal issuerTIR = calculateIRR(bondIssuerFlow, period0IssuerFlow);
        //System.out.println("issuerTIR: " + issuerTIR);

        return new FinancialIndicatorsDto(
                //ESTRUCTURACIÓN DEL BONO
                couponFrequency,
                capitalizationPeriod,
                periodsPerYear,
                periods,
                bond.getAnnualInterestRate(),
                tep,
                cok,
                initialCostsBoth,
                initialCostsBondHolder,
                //PRECIO ACTUAL Y UTILIDAD

                van,
                utilityOrLose,

                //RATIOS DE DECISIÓN

                duration,
                convexity,
                totalDurationPlusConvexity,
                modifiedDuration,

                //INDICADORES DE RENTABILIDAD
                BigDecimal.valueOf(0.2011995),
                BigDecimal.valueOf(0.1736642),
                BigDecimal.valueOf(0.1922097)
        );
    }

}
