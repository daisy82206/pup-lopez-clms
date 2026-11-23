package com.puplopez.clms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.puplopez.clms.model.Report;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findAllByOrderByReportDateDesc();
    List<Report> findByComputerLaboratoryLabIdOrderByReportDateDesc(Integer labId);
}