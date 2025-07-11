package com.grupo4.americash.service.impl;

import com.grupo4.americash.repository.BondRepository;
import com.grupo4.americash.repository.UserRepository;
import com.grupo4.americash.service.BondCalculationService;
import com.grupo4.americash.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BondServiceImplTest {

    @Mock
    private BondRepository bondRepository;

    @Mock
    private UserService userService;

    @Mock
    private BondCalculationService bondCalculationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BondServiceImpl bondService;

    @Test
    void deleteBond_shouldDeleteBond_whenBondExists() {
        // Arrange
        Long bondId = 1L;
        when(bondRepository.existsById(bondId)).thenReturn(true);

        // Act
        bondService.deleteBond(bondId);

        // Assert
        verify(bondRepository).deleteById(bondId);
    }

    @Test
    void deleteBond_shouldThrowException_whenBondDoesNotExist() {
        // Arrange
        Long bondId = 1L;
        when(bondRepository.existsById(bondId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> bondService.deleteBond(bondId));
    }
}
