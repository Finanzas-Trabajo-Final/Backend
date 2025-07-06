package com.grupo4.americash.service.impl;

import com.grupo4.americash.dto.BondDto;
import com.grupo4.americash.dto.BondRequest;
import com.grupo4.americash.entity.*;
import com.grupo4.americash.repository.BondRepository;
import com.grupo4.americash.repository.UserRepository;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.BondService;
import com.grupo4.americash.service.GracePeriodService;
import com.grupo4.americash.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BondServiceImpl implements BondService {
    private final GracePeriodService gracePeriodService;
    private final BondRepository bondRepository;
    private final UserService userService;
    private final BondCalculationService bondCalculationService;
    private final UserRepository userRepository;

    @Override
    public void deleteBond(Long id) {
        if (!bondRepository.existsById(id)) {
            throw new EntityNotFoundException("Bond not found");
        }
        bondRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<Bond> createBond(BondRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("USERNAME FROM SECURITY CONTEXT: " + username);
        System.out.println("ALL USERS IN DB: " + userRepository.findAll());
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        Bond bond = Bond.builder()
                .issuer(request.issuer())
                .faceValue(request.faceValue())
                .commercialValue(request.commercialValue())
                .currency(Currency.valueOf(request.currency()))
                .interestRateType(InterestRateType.valueOf(request.interestRateType()))
                .annualInterestRate(request.annualInterestRate())
                .capitalizationPeriod(request.capitalizationPeriod())
                .termInYears(request.termInYears())
                .paymentFrequencyInMonths(request.paymentFrequencyInMonths())
                .totalGraceMonths(request.totalGraceMonths())
                .partialGraceMonths(request.partialGraceMonths())
                .disbursementDate(request.disbursementDate())
                .discountRate(request.discountRate())
                .incomeTaxRate(request.incomeTaxRate())
                .premiumPercentage(request.premiumPercentage())
                .structuringCostPercentage(request.structuringCostPercentage())
                .placementCostPercentage(request.placementCostPercentage())
                .flotationCostPercentage(request.flotationCostPercentage())
                .cavaliCostPercentage(request.cavaliCostPercentage())
                .build();

        // Save first to generate ID
        bond.setUser(user);
        bond = bondRepository.save(bond);

        List<PaymentSchedule> schedule = bondCalculationService.generateSchedule(bond);
        schedule = gracePeriodService.applyGracePeriods(bond, schedule);

        // Save to bond if you want
        bond.setSchedule(schedule); // Optional: only if Bond entity has schedule list

        // Recalculate metrics after schedule

//bond.setDuration(bondCalculationService.calculateDuration(bond));


        return Optional.of(bondRepository.save(bond)); // Persist metrics
    }

    @Override
    public Optional<Bond> getBondById(Long id) {
        return bondRepository.findById(id);
    }

    @Override
    public Optional<Bond> updateBond(Long id, BondRequest request) {
        Bond existing = bondRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bond not found"));

        existing.setIssuer(request.issuer());
        existing.setFaceValue(request.faceValue());
        existing.setCommercialValue(request.commercialValue());
        existing.setCurrency(Currency.valueOf(request.currency()));
        existing.setInterestRateType(InterestRateType.valueOf(request.interestRateType()));
        existing.setAnnualInterestRate(request.annualInterestRate());
        existing.setCapitalizationPeriod(request.capitalizationPeriod());
        existing.setTermInYears(request.termInYears());
        existing.setPaymentFrequencyInMonths(request.paymentFrequencyInMonths());
        existing.setTotalGraceMonths(request.totalGraceMonths());
        existing.setPartialGraceMonths(request.partialGraceMonths());
        existing.setDisbursementDate(request.disbursementDate());

        return Optional.of(bondRepository.save(existing));
    }

    @Override
    public List<BondDto> getBondsByUserId(Long userId) {
        return bondRepository.findByUserId(userId)
                .stream()
                .map(BondDto::new)
                .toList();
    }

}
