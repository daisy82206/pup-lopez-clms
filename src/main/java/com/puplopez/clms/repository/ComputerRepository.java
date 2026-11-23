package com.puplopez.clms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.puplopez.clms.model.Computer;

public interface ComputerRepository extends JpaRepository<Computer, Integer> {
    Optional<Computer> findByLaboratoryLabIdAndComputerNumberIgnoreCase(Integer labId, String computerNumber);
    long countByStatus(String status);
}
