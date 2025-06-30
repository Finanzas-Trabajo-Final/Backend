package com.grupo4.americash.controller;

import com.grupo4.americash.dto.BondDto;
import com.grupo4.americash.dto.BondRequest;
import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.BondService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Bonds", description = "Endpoints for managing bonds")
@RequestMapping("api/v1/bonds")
@AllArgsConstructor
public class BondController {
    private BondService bondService;
    private BondCalculationService bondCalculationService;

    @PostMapping
    public ResponseEntity<BondDto>createBond(@Valid @RequestBody BondRequest bondRequest) {
        Bond bond = bondService.createBond(bondRequest)
                .orElseThrow(() -> new RuntimeException("Error creating bond"));

        return ResponseEntity.ok(new BondDto(bond));
    }

    // Get full payment schedule
    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<PaymentSchedule>> getSchedule(@PathVariable Long id) {
        Bond bond = bondService.getBondById(id)
                .orElseThrow(() -> new RuntimeException("Bond not found"));
        List<PaymentSchedule> schedule = bondCalculationService.generateSchedule(bond);
        return ResponseEntity.ok(schedule);
    }




}
