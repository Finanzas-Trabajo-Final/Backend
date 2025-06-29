package com.grupo4.americash.service;

import com.grupo4.americash.dto.BondDto;
import com.grupo4.americash.dto.BondRequest;
import com.grupo4.americash.entity.Bond;

import java.util.List;
import java.util.Optional;

public interface BondService {

    void deleteBond(Long id);


    Optional<Bond> createBond(BondRequest request);

    Optional<Bond> getBondById(Long id);

    Optional<Bond> updateBond(Long id, BondRequest request);

    List<BondDto> getBondsByUserId(Long userId);
}
