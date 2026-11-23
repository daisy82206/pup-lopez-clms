package com.puplopez.clms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.puplopez.clms.model.MaintenanceLog;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Integer> {
    List<MaintenanceLog> findAllByOrderByMaintenanceDateDesc();
}