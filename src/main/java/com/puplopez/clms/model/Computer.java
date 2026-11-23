package com.puplopez.clms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "computers")
public class Computer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer computerId;

    @ManyToOne
    @JoinColumn(name = "lab_id", nullable = false)
    private Laboratory laboratory;

    @Column(nullable = false, length = 20)
    private String computerNumber;

    @Column(length = 30)
    private String status = "Working";

    public Integer getComputerId() { return computerId; }
    public void setComputerId(Integer computerId) { this.computerId = computerId; }
    public Laboratory getLaboratory() { return laboratory; }
    public void setLaboratory(Laboratory laboratory) { this.laboratory = laboratory; }
    public String getComputerNumber() { return computerNumber; }
    public void setComputerNumber(String computerNumber) { this.computerNumber = computerNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}