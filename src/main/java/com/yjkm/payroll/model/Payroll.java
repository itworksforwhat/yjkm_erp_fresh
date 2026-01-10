package com.yjkm.payroll.model;

import jakarta.persistence.*;
import java.time.YearMonth;

@Entity
@Table(name = "payroll")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private Integer year;          // 연도

    @Column(nullable = false)
    private Integer month;         // 월

    // 근무 정보
    private Integer workDays;       // 근무일수
    private Double workHours;       // 근무시간
    private Double overtimeHours;   // 잔업시간
    private Double nightHours;      // 야간근무시간

    // 급여 정보
    private Integer basePay;        // 기본급
    private Integer overtimePay;    // 잔업수당
    private Integer nightPay;       // 야간수당
    private Integer holidayPay;     // 휴일수당
    private Integer bonusPay = 0;   // 상여금
    private Integer otherAllowance = 0; // 기타수당

    // 공제 정보
    private Integer pensionDeduction = 0;    // 국민연금
    private Integer healthDeduction = 0;    // 건강보험
    private Integer employmentDeduction = 0; // 고용보험
    private Integer socialDeduction = 0;    // 사우회비
    private Integer incomeTax = 0;          // 소득세
    private Integer localTax = 0;           // 지방세
    private Integer otherDeduction = 0;     // 기타공제

    // 최종 급여
    private Integer totalIncome = 0;  // 총소득
    private Integer totalDeduction = 0; // 총공제
    private Integer netPay = 0;        // 실수령액

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getWorkDays() { return workDays; }
    public void setWorkDays(Integer workDays) { this.workDays = workDays; }

    public Double getWorkHours() { return workHours; }
    public void setWorkHours(Double workHours) { this.workHours = workHours; }

    public Double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(Double overtimeHours) { this.overtimeHours = overtimeHours; }

    public Double getNightHours() { return nightHours; }
    public void setNightHours(Double nightHours) { this.nightHours = nightHours; }

    public Integer getBasePay() { return basePay; }
    public void setBasePay(Integer basePay) { this.basePay = basePay; }

    public Integer getOvertimePay() { return overtimePay; }
    public void setOvertimePay(Integer overtimePay) { this.overtimePay = overtimePay; }

    public Integer getNightPay() { return nightPay; }
    public void setNightPay(Integer nightPay) { this.nightPay = nightPay; }

    public Integer getHolidayPay() { return holidayPay; }
    public void setHolidayPay(Integer holidayPay) { this.holidayPay = holidayPay; }

    public Integer getBonusPay() { return bonusPay; }
    public void setBonusPay(Integer bonusPay) { this.bonusPay = bonusPay; }

    public Integer getOtherAllowance() { return otherAllowance; }
    public void setOtherAllowance(Integer otherAllowance) { this.otherAllowance = otherAllowance; }

    public Integer getPensionDeduction() { return pensionDeduction; }
    public void setPensionDeduction(Integer pensionDeduction) { this.pensionDeduction = pensionDeduction; }

    public Integer getHealthDeduction() { return healthDeduction; }
    public void setHealthDeduction(Integer healthDeduction) { this.healthDeduction = healthDeduction; }

    public Integer getEmploymentDeduction() { return employmentDeduction; }
    public void setEmploymentDeduction(Integer employmentDeduction) { this.employmentDeduction = employmentDeduction; }

    public Integer getSocialDeduction() { return socialDeduction; }
    public void setSocialDeduction(Integer socialDeduction) { this.socialDeduction = socialDeduction; }

    public Integer getIncomeTax() { return incomeTax; }
    public void setIncomeTax(Integer incomeTax) { this.incomeTax = incomeTax; }

    public Integer getLocalTax() { return localTax; }
    public void setLocalTax(Integer localTax) { this.localTax = localTax; }

    public Integer getOtherDeduction() { return otherDeduction; }
    public void setOtherDeduction(Integer otherDeduction) { this.otherDeduction = otherDeduction; }

    public Integer getTotalIncome() { return totalIncome; }
    public void setTotalIncome(Integer totalIncome) { this.totalIncome = totalIncome; }

    public Integer getTotalDeduction() { return totalDeduction; }
    public void setTotalDeduction(Integer totalDeduction) { this.totalDeduction = totalDeduction; }

    public Integer getNetPay() { return netPay; }
    public void setNetPay(Integer netPay) { this.netPay = netPay; }

    // 계산 메서드
    public void calculatePayroll() {
        totalIncome = (basePay != null ? basePay : 0) +
                      (overtimePay != null ? overtimePay : 0) +
                      (nightPay != null ? nightPay : 0) +
                      (holidayPay != null ? holidayPay : 0) +
                      (bonusPay != null ? bonusPay : 0) +
                      (otherAllowance != null ? otherAllowance : 0);

        totalDeduction = (pensionDeduction != null ? pensionDeduction : 0) +
                        (healthDeduction != null ? healthDeduction : 0) +
                        (employmentDeduction != null ? employmentDeduction : 0) +
                        (socialDeduction != null ? socialDeduction : 0) +
                        (incomeTax != null ? incomeTax : 0) +
                        (localTax != null ? localTax : 0) +
                        (otherDeduction != null ? otherDeduction : 0);

        netPay = totalIncome - totalDeduction;
    }
}
