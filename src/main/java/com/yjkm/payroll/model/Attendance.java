package com.yjkm.payroll.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate workDate;     // 근무일

    private LocalTime checkIn;      // 출근시간
    private LocalTime checkOut;     // 퇴근시간

    private Integer workMinutes;    // 근무분(분 단위)
    private Integer overtimeMinutes; // 잔업분
    private Integer nightMinutes;   // 야간근무분
    private Boolean isHoliday;      // 휴일 여부

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }

    public LocalTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalTime checkIn) { this.checkIn = checkIn; }

    public LocalTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalTime checkOut) { this.checkOut = checkOut; }

    public Integer getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { this.workMinutes = workMinutes; }

    public Integer getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Integer overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }

    public Integer getNightMinutes() { return nightMinutes; }
    public void setNightMinutes(Integer nightMinutes) { this.nightMinutes = nightMinutes; }

    public Boolean getIsHoliday() { return isHoliday; }
    public void setIsHoliday(Boolean holiday) { isHoliday = holiday; }
}
