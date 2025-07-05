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
            BigDecimal exponent = BigDecimal.valueOf(bond.getPaymentFrequencyInMonths() * 30)
                    .divide(BigDecimal.valueOf(360), mc);
            BigDecimal result = power(tea, exponent).subtract(BigDecimal.ONE);
            System.out.println(";;;;;;"+result);
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

//TODO
    @Override
    public List<PaymentSchedule> generateSchedule(Bond bond) {
        List<PaymentSchedule> schedule = new ArrayList<>();
        int periods = bond.getTermInMonths() / bond.getPaymentFrequencyInMonths();

        BigDecimal rate = calculateInflationAdjustment(bond);
        BigDecimal tep = calculateTXP(bond);
        BigDecimal initialBothFlowValue = bond.getStructuringCostPercentage().add(bond.getPlacementCostPercentage()).add(bond.getFlotationCostPercentage().add(bond.getCavaliCostPercentage())) ;
        BigDecimal initialBondHolderFlowValue =bond.getStructuringCostPercentage().add(bond.getPlacementCostPercentage());

        BigDecimal discountRate = bond.getDiscountRate();
        int termInDays = bond.getTermInMonths() * 30;
        BigDecimal daysDifferenceMagnitude = BigDecimal.valueOf(termInDays).divide(BigDecimal.valueOf(360), mc);
        BigDecimal COK = power(BigDecimal.ONE.add(discountRate), daysDifferenceMagnitude).subtract(BigDecimal.ONE);
        System.out.println("COK: "+COK);


        for (int i = 1; i <= periods; i++) {


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


            if(i== periods) ps.setQuota(ps.getCoupon().add(ps.getAmortization()));
            else ps.setQuota(ps.getCoupon());

            ps.setTaxShield(bond.getIncomeTaxRate().negate().multiply(ps.getCoupon()).setScale(4, RoundingMode.HALF_UP));
            //TODO: cambiar el flujo para seguir esta operacin =SI(A28<=L$7,I28+K28,0)

            ps.setIssuerFlow(ps.getQuota().add(ps.getPremium() != null ? ps.getPremium() : BigDecimal.ZERO));
            ps.setIssuerFlowWithShield(ps.getIssuerFlow().add(ps.getTaxShield()).setScale(6, RoundingMode.HALF_UP));
            ps.setBondholderFlow(ps.getIssuerFlow().negate());
//            ps.setDiscountedFlow(power(ps.getBondholderFlow(), BigDecimal.ONE.add(COK).multiply(BigDecimal.valueOf(i))));
            ps.setDiscountedFlow(BigDecimal.ZERO);

            ps.setFlowByTerm(ps.getBondholderFlow().multiply(BigDecimal.valueOf(i)).multiply(daysDifferenceMagnitude));
            ps.setConvexityFactor(ps.getDiscountedFlow().multiply(BigDecimal.valueOf(i)).multiply(BigDecimal.valueOf(i+1), mc));



            ps.setBond(bond);

            //VALORES QUE SON 0 HASTA EL ULTIMO PERIODO
            ps.setPremium(i == periods ? bond.getPremiumPercentage().multiply(bond.getFaceValue()) : BigDecimal.ZERO);
            ps.setAmortization(i == periods ? ps.getIndexedBondValue().negate().setScale(6, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            schedule.add(ps);


        }

        return gracePeriodService.applyGracePeriods(bond, schedule);
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
