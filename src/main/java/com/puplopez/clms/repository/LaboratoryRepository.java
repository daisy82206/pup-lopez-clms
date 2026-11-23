package com.puplopez.clms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.puplopez.clms.model.Laboratory;

public interface LaboratoryRepository extends JpaRepository<Laboratory, Integer> {
}