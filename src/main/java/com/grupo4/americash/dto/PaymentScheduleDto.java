package com.grupo4.americash.dto;

import com.grupo4.americash.entity.PaymentSchedule;

import java.math.BigDecimal;

public record PaymentScheduleDto(
        Integer period,
        BigDecimal scheduledDateInflationAnnual,
        BigDecimal scheduledDateInflationPeriod,
        String graceType,

        BigDecimal bondValue,
        BigDecimal indexedBondValue,
        BigDecimal coupon,
        BigDecimal quota,
        BigDecimal amortization,
        BigDecimal premium,
        BigDecimal taxShield,
        //Flujos
        BigDecimal issuerFlow,
        BigDecimal issuerFlowWithShield,
        BigDecimal bondholderFlow,
        BigDecimal discountedFlow,
        BigDecimal flowByTerm,
        //Convexidad
        BigDecimal convexityFactor
) {
    public PaymentScheduleDto(PaymentSchedule p) {
        this(
                p.getPeriod(),
                p.getScheduledDateInflationAnnual(),
                p.getScheduledDateInflationPeriod(),
                p.getGraceType(),
                p.getBondValue(),
                p.getIndexedBondValue(),
                p.getCoupon(),
                p.getQuota(),
                p.getAmortization(),
                p.getPremium(),
                p.getTaxShield(),
                p.getIssuerFlow(),
                p.getIssuerFlowWithShield(),
                p.getBondholderFlow(),
                //FLUJO ACT
                p.getDiscountedFlow(),
                p.getFlowByTerm(),
                p.getConvexityFactor()
        );
    }
}