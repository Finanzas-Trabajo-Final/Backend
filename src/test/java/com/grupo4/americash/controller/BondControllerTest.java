package com.grupo4.americash.controller;

import com.grupo4.americash.dto.BondDto;
import com.grupo4.americash.dto.BondRequest;
import com.grupo4.americash.dto.FinancialIndicatorsDto;
import com.grupo4.americash.dto.PaymentScheduleDto;
import com.grupo4.americash.entity.*;
import com.grupo4.americash.entity.Bond;
import com.grupo4.americash.entity.PaymentSchedule;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.BondService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDate;
import com.grupo4.americash.entity.Currency;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class BondControllerTest {

    private BondService bondService;
    private BondCalculationService bondCalculationService;
    private BondController bondController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bondService = Mockito.mock(BondService.class);
        bondCalculationService = Mockito.mock(BondCalculationService.class);
        bondController = new BondController(bondService, bondCalculationService);
        mockMvc = MockMvcBuilders.standaloneSetup(bondController).build();

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCreateBond() throws Exception {
        BondRequest bondRequest = new BondRequest(
                "Banco de América",
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(98.00),
                "EFECTIVA",
                BigDecimal.valueOf(0.08),
                60,
                5,
                6,
                0,
                0,
                "PEN",
                BigDecimal.valueOf(0.045),
                BigDecimal.valueOf(0.30),
                LocalDate.of(2025, 1, 1), // fixed here
                BigDecimal.valueOf(0.01),
                BigDecimal.valueOf(0.01),
                BigDecimal.valueOf(0.0025),
                BigDecimal.valueOf(0.0045),
                BigDecimal.valueOf(0.005)
        );
        User user = new User();
        user.setUsername("jhondoe");
        Bond bond = new Bond();
        bond.setId(1L);
        bond.setIssuer("Peru Gov");
        bond.setCurrency(Currency.PEN); // fixed here
        bond.setInterestRateType(InterestRateType.EFECTIVA);
        bond.setUser(user); // Set a valid user

        Mockito.when(bondService.createBond(any())).thenReturn(Optional.of(bond));

        mockMvc.perform(post("/api/v1/bonds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bondRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetPaymentSchedule() throws Exception {
        Bond bond = new Bond();
        bond.setId(1L);

        PaymentSchedule ps = new PaymentSchedule();
        ps.setPeriod(1);
        ps.setCoupon(BigDecimal.valueOf(100));

        Mockito.when(bondService.getBondById(1L)).thenReturn(Optional.of(bond));
        Mockito.when(bondCalculationService.generateSchedule(bond)).thenReturn(List.of(ps));

        mockMvc.perform(get("/api/v1/bonds/1/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value(1))
                .andExpect(jsonPath("$[0].coupon").value(100));
    }

    @Test
    void shouldGetFinancialIndicators() throws Exception {
        Bond bond = new Bond();
        bond.setId(1L);

        FinancialIndicatorsDto dto = new FinancialIndicatorsDto(
            1, 2, 3, 4,
            BigDecimal.valueOf(0.12), BigDecimal.valueOf(0.10), BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            BigDecimal.ZERO, BigDecimal.ZERO
        );

        Mockito.when(bondService.getBondById(1L)).thenReturn(Optional.of(bond));
        Mockito.when(bondCalculationService.getFinancialIndicators(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/bonds/1/financial-indicators"))
                .andExpect(status().isOk());
    }
}