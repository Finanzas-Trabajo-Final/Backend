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
        }else {
            BigDecimal nominalRate = bond.getAnnualInterestRate(); // Ej: 0.12 (12% anual)
            int capitalizationMonths = bond.getCapitalizationPeriod(); // Ej: 3 si es trimestral
            int periodsPerYear = 360 / capitalizationMonths; // Número de capitalizaciones por año

            // TEA = (1 + TNP/m)^m - 1
            BigDecimal onePlusRate = BigDecimal.ONE.add(nominalRate.divide(BigDecimal.valueOf(periodsPerYear), mc));
            BigDecimal tea = power(onePlusRate, BigDecimal.valueOf(periodsPerYear)).subtract(BigDecimal.ONE);

            System.out.println("TEA calculada desde TNP: " + tea);
            return tea;
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
            BigDecimal denominator = BigDecimal.ONE.add(discountRate).pow(i , mc);
            van = van.add(numerator.divide(denominator, mc));
        }

        return van.setScale(6, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateIRRWithBisection(List<BigDecimal> cashFlows, BigDecimal initialFlow, MathContext mc) {
        // Insertamos el flujo inicial (negativo) como primer elemento
        List<BigDecimal> allFlows = new ArrayList<>();
        allFlows.add(initialFlow); // Ej. -980.00
        allFlows.addAll(cashFlows); // Flujos de schedule

        BigDecimal low = BigDecimal.valueOf(-0.9999); // mínimo -99.99%
        BigDecimal high = BigDecimal.ONE;             // máximo 100%
        BigDecimal guess = BigDecimal.ZERO;
        BigDecimal tolerance = new BigDecimal("0.0000001");
        int maxIterations = 100;

        for (int i = 0; i < maxIterations; i++) {
            guess = low.add(high).divide(BigDecimal.valueOf(2), mc);
            BigDecimal npv = calculateNPV(allFlows, guess, mc);

            if (npv.abs().compareTo(tolerance) < 0) break;

            if (npv.compareTo(BigDecimal.ZERO) > 0) {
                low = guess;
            } else {
                high = guess;
            }
        }

        return guess;
    }

    private BigDecimal calculateNPV(List<BigDecimal> flows, BigDecimal rate, MathContext mc) {
        BigDecimal npv = BigDecimal.ZERO;
        for (int t = 0; t < flows.size(); t++) {
            BigDecimal denominator = BigDecimal.ONE.add(rate).pow(t, mc);
            npv = npv.add(flows.get(t).divide(denominator, mc));
        }
        return npv;
    }
    private List<BigDecimal> negateList(List<BigDecimal> list) {
        return list.stream().map(BigDecimal::negate).toList();
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


        int totalGracePeriods = bond.getTotalGraceMonths() > 0 ? periods / bond.getTotalGraceMonths() : 0;
        int partialGracePeriods = bond.getPartialGraceMonths() > 0 ? periods / bond.getPartialGraceMonths() : 0;

        System.out.println("TOTAL GRACE PERIODS: " + totalGracePeriods + ", PARTIAL GRACE PERIODS: " + partialGracePeriods);

        for (int i = 1; i <= periods; i++) {
            PaymentSchedule ps = new PaymentSchedule();
            ps.setPeriod(i);
            ps.setScheduledDateInflationAnnual(BigDecimal.valueOf(0.1));
            ps.setScheduledDateInflationPeriod(rate.setScale(4, RoundingMode.HALF_UP));

            if (totalGracePeriods == 0 && partialGracePeriods == 0) {
                ps.setGraceType("S");
            } else if (i < totalGracePeriods) {
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
        int capitalizationPeriod = bond.getCapitalizationPeriod(); // Convertir a días
        System.out.println(bond.getCapitalizationPeriod());
        int periods = BigDecimal.valueOf(periodsPerYear).multiply(BigDecimal.valueOf(bond.getTermInYears())).intValue();


        BigDecimal tep = calculateTXP(bond);
        System.out.println("TEP: " + tep);

        BigDecimal initialCostsBothPercentage = bond.getStructuringCostPercentage().add(bond.getPlacementCostPercentage()).add(bond.getFlotationCostPercentage().add(bond.getCavaliCostPercentage())) ;
        BigDecimal initialCostsBoth = bond.getCommercialValue().multiply(initialCostsBothPercentage).setScale(6, RoundingMode.HALF_UP);


        BigDecimal initialCostsPercentageBondHolder =bond.getFlotationCostPercentage().add(bond.getCavaliCostPercentage());
        BigDecimal initialCostsBondHolder = bond.getCommercialValue().multiply(initialCostsPercentageBondHolder).setScale(6, RoundingMode.HALF_UP);


        BigDecimal period0IssuerFlow = bond.getCommercialValue().subtract(initialCostsBoth);
        BigDecimal period0BondholderFlow = bond.getCommercialValue().negate().subtract(initialCostsBondHolder);



        BigDecimal discountRate = bond.getDiscountRate();
        int paymentFrequencyInDays = bond.getPaymentFrequencyInMonths() * 30;
        BigDecimal daysDifferenceMagnitude = BigDecimal.valueOf(paymentFrequencyInDays).divide(BigDecimal.valueOf(360), mc);
        BigDecimal cok = power(BigDecimal.ONE.add(discountRate), daysDifferenceMagnitude).subtract(BigDecimal.ONE);

        // VAN
        BigDecimal van = calculateVAN(bondHolderFlow, cok);

        BigDecimal utilityOrLose = period0BondholderFlow.add(van);



        //RATIOS DE DECISIÓN
        BigDecimal duration = periodFlowByTerm.divide(periodDiscountedFlow, mc);

        BigDecimal rate =BigDecimal.valueOf(360).divide(BigDecimal.valueOf(couponFrequency), mc);
        BigDecimal rateSquared = rate.pow(2, mc);
        BigDecimal cokSqr = BigDecimal.ONE.add(cok).pow(2,mc);

        BigDecimal multipliedFactor = rateSquared.multiply(cokSqr).multiply(periodDiscountedFlow);


        BigDecimal convexity = convexityFactorPeriodFlow.divide(multipliedFactor, mc);

        BigDecimal totalDurationPlusConvexity = convexity.add(duration);

        BigDecimal modifiedDuration = duration.divide(BigDecimal.ONE.add(cok),mc);

        //INDICADORES DE RENTABILIDAD
        BigDecimal bondIssuerTIR = calculateIRRWithBisection(
                negateList(bondIssuerFlow),
                period0IssuerFlow.negate(),
                mc
        ).add(BigDecimal.ONE);


        BigDecimal bondIssuerTIRWithShield = calculateIRRWithBisection(
                negateList(bondIssuerWithShieldFlow),
                period0IssuerFlow.negate(),
                mc
        ).add(BigDecimal.ONE);

        BigDecimal issuerTCEA = bondIssuerTIR.pow(periodsPerYear).subtract(BigDecimal.ONE);

        BigDecimal bondHolderTIR = calculateIRRWithBisection(
                bondHolderFlow,
                period0BondholderFlow,
                mc
        ).add(BigDecimal.ONE);


        BigDecimal bondHolderTCEA = bondHolderTIR.pow(periodsPerYear).subtract(BigDecimal.ONE);

        BigDecimal bondHolderTCEAWithShield = bondIssuerTIRWithShield.pow(periodsPerYear).subtract(BigDecimal.ONE);


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
                issuerTCEA,
                bondHolderTCEAWithShield,
                bondHolderTCEA
        );
    }

}
