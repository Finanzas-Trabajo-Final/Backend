package com.grupo4.americash.repository;

import com.grupo4.americash.entity.PaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, Long> {
    List<PaymentSchedule> findByBondId(Long bondId);
}
