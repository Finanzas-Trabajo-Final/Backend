package com.grupo4.americash.dto;

import com.grupo4.americash.entity.PaymentSchedule;

import java.math.BigDecimal;

public record PaymentScheduleDto(
        Integer period,
        BigDecimal coupon,
        BigDecimal amortization,
        BigDecimal quota,
        BigDecimal scheduledDateInflationAnnual,
        BigDecimal scheduledDateInflationPeriod,
        String graceType,
        BigDecimal bondValue,
        BigDecimal indexedBondValue,
        BigDecimal premium,
        BigDecimal taxShield,
        BigDecimal issuerFlow,
        BigDecimal issuerFlowWithShield,
        BigDecimal bondholderFlow,
        BigDecimal discountedFlow,
        BigDecimal flowByTerm,
        BigDecimal convexityFactor
) {
    public PaymentScheduleDto(PaymentSchedule p) {
        this(
                p.getPeriod(),
                p.getCoupon(),
                p.getAmortization(),
                p.getQuota(),
                p.getScheduledDateInflationAnnual(),
                p.getScheduledDateInflationPeriod(),
                p.getGraceType(),
                p.getBondValue(),
                p.getIndexedBondValue(),
                p.getPremium(),
                p.getTaxShield(),
                p.getIssuerFlow(),
                p.getIssuerFlowWithShield(),
                p.getBondholderFlow(),
                p.getDiscountedFlow(),
                p.getFlowByTerm(),
                p.getConvexityFactor()
        );
    }
}