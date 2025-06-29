package com.grupo4.americash.repository;

import com.grupo4.americash.entity.Bond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BondRepository extends JpaRepository<Bond, Long> {


    // Get all bonds created by a specific user
    List<Bond> findByUserId(Long userId);

    // Optional: if you want to search by issuer name
    List<Bond> findByIssuerContainingIgnoreCase(String issuer);
}
