package com.yjkm.payroll.service;

import com.yjkm.payroll.model.Employee;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmployeeImportService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeImportService.class);
    private final EmployeeService employeeService = new EmployeeService();

    /**
     * 파일 형식 자동 감지 및 임포트
     */
    public int importFromFile(File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".csv")) {
            return importFromCSV(file);
        } else if (fileName.endsWith(".txt")) {
            return importFromTXT(file);
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return importFromExcel(file);
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 형식: " + fileName);
        }
    }

    /**
     * CSV 파일 임포트
     * 형식: 직원번호,이름,부서,직급,시급,입사일
     */
    private int importFromCSV(File file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // 첨번째 줄은 헤더로 간주 (선택사항)
                if (firstLine) {
                    firstLine = false;
                    if (line.contains("직원번호") || line.contains("이름")) {
                        continue; // 헤더 스킬
                    }
                }
                Employee emp = parseCSVLine(line);
                if (emp != null) {
                    employees.add(emp);
                }
            }
        }
        return saveAll(employees);
    }

    /**
     * TXT 파일 임포트 (탭 구분 또는 쉼표 구분)
     */
    private int importFromTXT(File file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.contains("직원번호") || line.contains("이름")) {
                        continue;
                    }
                }
                Employee emp = parseTXTLine(line);
                if (emp != null) {
                    employees.add(emp);
                }
            }
        }
        return saveAll(employees);
    }

    /**
     * 엑셀 파일 임포트
     */
    private int importFromExcel(File file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue; // 헤더 스킬
                }
                Employee emp = parseExcelRow(row);
                if (emp != null) {
                    employees.add(emp);
                }
            }
        }
        return saveAll(employees);
    }

    private Employee parseCSVLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 5) return null;

            Employee emp = new Employee();
            emp.setEmployeeNumber(parts[0].trim());
            emp.setName(parts[1].trim());
            emp.setDepartment(parts[2].trim());
            emp.setPosition(parts[3].trim());
            emp.setHourlyWage(Integer.parseInt(parts[4].trim()));
            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                emp.setHireDate(LocalDate.parse(parts[5].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            emp.setIsActive(true);
            return emp;
        } catch (Exception e) {
            logger.warn("줄 파싱 실패: {}", line);
            return null;
        }
    }

    private Employee parseTXTLine(String line) {
        try {
            // 탭 또는 쉼표로 구분
            String[] parts = line.contains("\t") ? line.split("\t") : line.split(",");
            if (parts.length < 5) return null;

            Employee emp = new Employee();
            emp.setEmployeeNumber(parts[0].trim());
            emp.setName(parts[1].trim());
            emp.setDepartment(parts[2].trim());
            emp.setPosition(parts[3].trim());
            emp.setHourlyWage(Integer.parseInt(parts[4].trim()));
            if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                emp.setHireDate(LocalDate.parse(parts[5].trim(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            emp.setIsActive(true);
            return emp;
        } catch (Exception e) {
            logger.warn("줄 파싱 실패: {}", line);
            return null;
        }
    }

    private Employee parseExcelRow(Row row) {
        try {
            Employee emp = new Employee();
            emp.setEmployeeNumber(getCellValue(row.getCell(0)));
            emp.setName(getCellValue(row.getCell(1)));
            emp.setDepartment(getCellValue(row.getCell(2)));
            emp.setPosition(getCellValue(row.getCell(3)));
            emp.setHourlyWage(Integer.parseInt(getCellValue(row.getCell(4))));
            
            String hireDate = getCellValue(row.getCell(5));
            if (hireDate != null && !hireDate.isEmpty()) {
                emp.setHireDate(LocalDate.parse(hireDate, DateTimeFormatter.ISO_LOCAL_DATE));
            }
            emp.setIsActive(true);
            return emp;
        } catch (Exception e) {
            logger.warn("행 파싱 실패: {}", row.getRowNum());
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private int saveAll(List<Employee> employees) {
        int count = 0;
        for (Employee emp : employees) {
            try {
                employeeService.saveEmployee(emp);
                count++;
            } catch (Exception e) {
                logger.error("직원 저장 실패: {}", emp.getName(), e);
            }
        }
        logger.info("✅ {} 명의 직원 임포트 완료", count);
        return count;
    }
}
