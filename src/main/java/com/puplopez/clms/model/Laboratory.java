package com.puplopez.clms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "laboratories")
public class Laboratory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer labId;

    @Column(nullable = false, length = 100)
    private String labName;

    public Integer getLabId() { return labId; }
    public void setLabId(Integer labId) { this.labId = labId; }
    public String getLabName() { return labName; }
    public void setLabName(String labName) { this.labName = labName; }
}