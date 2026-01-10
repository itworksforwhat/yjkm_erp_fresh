package com.yjkm.payroll.model;

import jakarta.persistence.*;

@Entity
@Table(name = "deductions")
public class Deduction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;           // 공제항목명 (국민연금, 건강보험, 고용보험, 사우회비 등)

    private Double rate;           // 공제율 (%)
    private Integer amount;        // 고정금액
    private String type;           // "PERCENTAGE" 또는 "FIXED"

    private Boolean isActive = true; // 활성화 여부

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}
