package com.grupo4.americash.controller;

import com.grupo4.americash.dto.BondDto;
import com.grupo4.americash.dto.BondRequest;
import com.grupo4.americash.entity.Bond;

import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.BondService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Bonds", description = "Endpoints for managing bonds")
@RequestMapping("api/v1/bonds")
@AllArgsConstructor
public class BondController {
    private BondService bondService;
    private BondCalculationService bondCalculationService;

    @PostMapping
    public ResponseEntity<BondDto>createBond(@Valid @RequestBody BondRequest bondRequest,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Bond bond = bondService.createBond(bondRequest, username)
                .orElseThrow(() -> new RuntimeException("Error creating bond"));

        return ResponseEntity.ok(new BondDto(bond));
    }

}
