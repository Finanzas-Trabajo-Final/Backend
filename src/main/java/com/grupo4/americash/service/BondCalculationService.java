package com.grupo4.americash.service;

import com.grupo4.americash.dto.FinancialIndicatorsDto;
import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;

import java.math.BigDecimal;
import java.util.List;

public interface BondCalculationService {
    List<PaymentSchedule> generateSchedule(Bond bond);



    FinancialIndicatorsDto getFinancialIndicators(Bond bond);

}
