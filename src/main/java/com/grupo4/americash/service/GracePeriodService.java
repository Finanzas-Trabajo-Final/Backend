package com.grupo4.americash.service;

import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;

import java.util.List;

public interface GracePeriodService {
    List<PaymentSchedule> applyGracePeriods(Bond bond, List<PaymentSchedule> originalSchedule);

}
