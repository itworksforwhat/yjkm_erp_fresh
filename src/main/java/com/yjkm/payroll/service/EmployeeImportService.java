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
import java.io.IOException;
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
     * 형식: 날짜,시간,사원번호,이름,부서코드,부서명,직급,주민번호,휴대폰,근무형태,... (최대 19개 필드)
     */
    private int importFromCSV(File file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.contains("날짜") || line.contains("사원번호") || line.contains("이름")) {
                        continue; // 헤더 스킵
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
     * TXT 파일 임포트 (탭 구분)
     */
    private int importFromTXT(File file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.contains("날짜") || line.contains("사원번호")) {
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
                    continue; // 헤더 스킵
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
            if (parts.length < 4) return null;

            Employee emp = new Employee();
            // 형식: 날짜(0), 시간(1), 사원번호(2), 이름(3), 부서코드(4), 부서명(5), 직급(6), ...
            
            emp.setEmployeeNumber(parts[2].trim());
            emp.setName(parts[3].trim());
            
            // 부서명 (6번 인덱스, 또는 5번 인덱스)
            if (parts.length > 5) {
                emp.setDepartment(parts[5].trim());
            }
            
            // 직급 (7번 인덱스, 또는 6번 인덱스)
            if (parts.length > 6) {
                emp.setPosition(parts[6].trim());
            }
            
            // 시급은 실제 데이터에 없으므로 기본값
            emp.setHourlyWage(0);
            emp.setIsActive(true);
            
            logger.debug("파싱 성공: {} ({})", emp.getName(), emp.getDepartment());
            return emp;
        } catch (Exception e) {
            logger.warn("줄 파싱 실패: {}", line);
            return null;
        }
    }

    private Employee parseTXTLine(String line) {
        try {
            // 탭으로 구분 (20-19개 필드)
            String[] parts = line.split("\t");
            if (parts.length < 4) return null;

            Employee emp = new Employee();
            
            // 형식: 날짜(0), 시간(1), 사원번호(2), 이름(3), 부서코드(4), 부서명(5), 직급(6), ...
            
            emp.setEmployeeNumber(parts[2].trim());
            emp.setName(parts[3].trim());
            
            if (parts.length > 5) {
                emp.setDepartment(parts[5].trim());
            }
            
            if (parts.length > 6) {
                emp.setPosition(parts[6].trim());
            }
            
            // 입사일이 있으면 파싱 (부서명에서 찾기 어려우므로 생략)
            emp.setHourlyWage(0);
            emp.setIsActive(true);
            
            logger.debug("파싱 성공: {} ({})", emp.getName(), emp.getDepartment());
            return emp;
        } catch (Exception e) {
            logger.warn("줄 파싱 실패: {}", line);
            return null;
        }
    }

    private Employee parseExcelRow(Row row) {
        try {
            Employee emp = new Employee();
            
            // 엑셀 셀 인덱스: 0=날짜, 1=시간, 2=사원번호, 3=이름, 4=부서코드, 5=부서명, 6=직급, ...
            
            emp.setEmployeeNumber(getCellValue(row.getCell(2)));
            emp.setName(getCellValue(row.getCell(3)));
            emp.setDepartment(getCellValue(row.getCell(5)));
            emp.setPosition(getCellValue(row.getCell(6)));
            emp.setHourlyWage(0);
            emp.setIsActive(true);
            
            logger.debug("파싱 성공: {} ({})", emp.getName(), emp.getDepartment());
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
                // 중복 확인 (사원번호 기준)
                if (emp.getEmployeeNumber() != null && !emp.getEmployeeNumber().isEmpty()) {
                    employeeService.saveEmployee(emp);
                    count++;
                }
            } catch (Exception e) {
                logger.error("직원 저장 실패: {}", emp.getName(), e);
            }
        }
        logger.info("✅ {} 명의 직원 임포트 완료", count);
        return count;
    }
}
