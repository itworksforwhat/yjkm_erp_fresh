package com.yjkm.payroll.service;

import com.yjkm.payroll.model.Attendance;
import com.yjkm.payroll.model.Employee;
import com.yjkm.payroll.model.SecomConfig;
import com.yjkm.payroll.util.HibernateUtil;
import com.yjkm.payroll.util.SecomConnectionUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 세콤 데이터 동기화 서비스
 * 세콤 데이터베이스에서 출퇴근 데이터를 가져와 로컬 DB에 저장합니다.
 */
public class SecomSyncService {
    private static final Logger logger = LoggerFactory.getLogger(SecomSyncService.class);

    /**
     * 세콤 데이터베이스에서 출퇴근 데이터 가져오기
     *
     * @param config    세콤 설정
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 가져온 출퇴근 데이터 리스트
     * @throws SQLException 데이터베이스 오류 시
     */
    public List<Map<String, Object>> fetchSecomData(SecomConfig config, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        String sql = buildSelectQuery(config, startDate, endDate);
        logger.info("세콤 데이터 조회 SQL: {}", sql);

        try (Connection conn = SecomConnectionUtil.getConnection(config);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            logger.info("세콤 데이터 {}건 조회 완료", results.size());
        }

        return results;
    }

    /**
     * SQL 쿼리 생성
     * 실제 세콤 DB 구조에 맞게 수정이 필요합니다.
     */
    private String buildSelectQuery(SecomConfig config, LocalDate startDate, LocalDate endDate) {
        String tableName = config.getTableName();

        // 일반적인 세콤 DB 구조를 가정한 쿼리
        // 실제 환경에서는 컬럼명을 확인하고 수정해야 합니다
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("사번, ");              // 또는 EMP_NO, EMPLOYEE_NO 등
        sql.append("성명, ");              // 또는 EMP_NAME, NAME 등
        sql.append("출입일시, ");          // 또는 INOUT_TIME, ACCESS_TIME 등
        sql.append("출입구분 ");           // 또는 INOUT_TYPE, ACCESS_TYPE 등 (출근/퇴근 구분)
        sql.append("FROM ").append(tableName).append(" ");
        sql.append("WHERE 출입일시 >= '").append(startDate).append("' ");
        sql.append("AND 출입일시 < '").append(endDate.plusDays(1)).append("' ");
        sql.append("ORDER BY 사번, 출입일시");

        return sql.toString();
    }

    /**
     * 세콤 데이터를 Attendance 엔티티로 변환하고 저장
     *
     * @param secomData 세콤 데이터
     * @return 저장된 출퇴근 기록 수
     */
    public int syncToAttendance(List<Map<String, Object>> secomData) {
        EntityManager em = HibernateUtil.getEntityManager();
        int savedCount = 0;

        try {
            em.getTransaction().begin();

            // 직원별, 날짜별로 데이터를 그룹화
            Map<String, Map<LocalDate, List<Map<String, Object>>>> groupedData = groupByEmployeeAndDate(secomData);

            for (Map.Entry<String, Map<LocalDate, List<Map<String, Object>>>> empEntry : groupedData.entrySet()) {
                String empNo = empEntry.getKey();

                // 사번으로 직원 찾기
                Employee employee = findEmployeeByNumber(em, empNo);
                if (employee == null) {
                    logger.warn("사번 {}에 해당하는 직원을 찾을 수 없습니다.", empNo);
                    continue;
                }

                for (Map.Entry<LocalDate, List<Map<String, Object>>> dateEntry : empEntry.getValue().entrySet()) {
                    LocalDate workDate = dateEntry.getKey();
                    List<Map<String, Object>> dayRecords = dateEntry.getValue();

                    // 해당 날짜의 출퇴근 기록이 이미 있는지 확인
                    Attendance existingAttendance = findAttendance(em, employee, workDate);

                    if (existingAttendance == null) {
                        // 새 출퇴근 기록 생성
                        Attendance attendance = createAttendanceFromSecomData(employee, workDate, dayRecords);
                        if (attendance != null) {
                            em.persist(attendance);
                            savedCount++;
                        }
                    } else {
                        // 기존 기록 업데이트
                        updateAttendanceFromSecomData(existingAttendance, dayRecords);
                        em.merge(existingAttendance);
                        savedCount++;
                    }
                }
            }

            em.getTransaction().commit();
            logger.info("세콤 데이터 {}건 동기화 완료", savedCount);

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("세콤 데이터 동기화 중 오류 발생", e);
            throw new RuntimeException("세콤 데이터 동기화 실패: " + e.getMessage(), e);
        } finally {
            em.close();
        }

        return savedCount;
    }

    /**
     * 직원별, 날짜별로 데이터 그룹화
     */
    private Map<String, Map<LocalDate, List<Map<String, Object>>>> groupByEmployeeAndDate(
            List<Map<String, Object>> secomData) {

        Map<String, Map<LocalDate, List<Map<String, Object>>>> grouped = new HashMap<>();

        for (Map<String, Object> record : secomData) {
            // 실제 컬럼명에 맞게 수정 필요
            String empNo = getStringValue(record, "사번", "EMP_NO", "EMPLOYEE_NO");
            LocalDateTime inoutTime = getDateTimeValue(record, "출입일시", "INOUT_TIME", "ACCESS_TIME");

            if (empNo == null || inoutTime == null) {
                continue;
            }

            LocalDate workDate = inoutTime.toLocalDate();

            grouped.computeIfAbsent(empNo, k -> new HashMap<>())
                    .computeIfAbsent(workDate, k -> new ArrayList<>())
                    .add(record);
        }

        return grouped;
    }

    /**
     * 세콤 데이터에서 Attendance 엔티티 생성
     */
    private Attendance createAttendanceFromSecomData(Employee employee, LocalDate workDate,
                                                     List<Map<String, Object>> dayRecords) {
        if (dayRecords.isEmpty()) {
            return null;
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setWorkDate(workDate);

        // 출근/퇴근 시간 찾기
        LocalTime checkIn = null;
        LocalTime checkOut = null;

        for (Map<String, Object> record : dayRecords) {
            LocalDateTime inoutTime = getDateTimeValue(record, "출입일시", "INOUT_TIME", "ACCESS_TIME");
            String inoutType = getStringValue(record, "출입구분", "INOUT_TYPE", "ACCESS_TYPE");

            if (inoutTime == null) {
                continue;
            }

            LocalTime time = inoutTime.toLocalTime();

            // 출입 구분에 따라 출근/퇴근 판단
            // "출근", "IN", "1" 등의 값으로 출근을 표시
            // "퇴근", "OUT", "2" 등의 값으로 퇴근을 표시
            if (inoutType != null && (inoutType.contains("출근") || inoutType.equals("IN") || inoutType.equals("1"))) {
                if (checkIn == null || time.isBefore(checkIn)) {
                    checkIn = time;
                }
            } else if (inoutType != null && (inoutType.contains("퇴근") || inoutType.equals("OUT") || inoutType.equals("2"))) {
                if (checkOut == null || time.isAfter(checkOut)) {
                    checkOut = time;
                }
            } else {
                // 출입 구분이 명확하지 않은 경우, 시간대로 판단
                if (checkIn == null) {
                    checkIn = time;
                } else {
                    checkOut = time;
                }
            }
        }

        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);

        // 근무 시간 계산
        if (checkIn != null && checkOut != null) {
            int workMinutes = (int) java.time.Duration.between(checkIn, checkOut).toMinutes();
            attendance.setWorkMinutes(workMinutes);
        }

        return attendance;
    }

    /**
     * 기존 출퇴근 기록 업데이트
     */
    private void updateAttendanceFromSecomData(Attendance attendance, List<Map<String, Object>> dayRecords) {
        // createAttendanceFromSecomData와 동일한 로직으로 출퇴근 시간 업데이트
        LocalTime checkIn = null;
        LocalTime checkOut = null;

        for (Map<String, Object> record : dayRecords) {
            LocalDateTime inoutTime = getDateTimeValue(record, "출입일시", "INOUT_TIME", "ACCESS_TIME");
            String inoutType = getStringValue(record, "출입구분", "INOUT_TYPE", "ACCESS_TYPE");

            if (inoutTime == null) {
                continue;
            }

            LocalTime time = inoutTime.toLocalTime();

            if (inoutType != null && (inoutType.contains("출근") || inoutType.equals("IN") || inoutType.equals("1"))) {
                if (checkIn == null || time.isBefore(checkIn)) {
                    checkIn = time;
                }
            } else if (inoutType != null && (inoutType.contains("퇴근") || inoutType.equals("OUT") || inoutType.equals("2"))) {
                if (checkOut == null || time.isAfter(checkOut)) {
                    checkOut = time;
                }
            } else {
                if (checkIn == null) {
                    checkIn = time;
                } else {
                    checkOut = time;
                }
            }
        }

        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);

        if (checkIn != null && checkOut != null) {
            int workMinutes = (int) java.time.Duration.between(checkIn, checkOut).toMinutes();
            attendance.setWorkMinutes(workMinutes);
        }
    }

    /**
     * 사번으로 직원 찾기
     */
    private Employee findEmployeeByNumber(EntityManager em, String empNo) {
        try {
            return em.createQuery("SELECT e FROM Employee e WHERE e.employeeNumber = :empNo", Employee.class)
                    .setParameter("empNo", empNo)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 출퇴근 기록 찾기
     */
    private Attendance findAttendance(EntityManager em, Employee employee, LocalDate workDate) {
        try {
            return em.createQuery(
                            "SELECT a FROM Attendance a WHERE a.employee = :emp AND a.workDate = :date",
                            Attendance.class)
                    .setParameter("emp", employee)
                    .setParameter("date", workDate)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Map에서 문자열 값 가져오기 (여러 키 시도)
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    /**
     * Map에서 날짜시간 값 가져오기 (여러 키 시도)
     */
    private LocalDateTime getDateTimeValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                if (value instanceof LocalDateTime) {
                    return (LocalDateTime) value;
                } else if (value instanceof Timestamp) {
                    return ((Timestamp) value).toLocalDateTime();
                } else if (value instanceof String) {
                    // 문자열인 경우 파싱 시도
                    try {
                        return LocalDateTime.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        logger.warn("날짜 파싱 실패: {}", value);
                    }
                }
            }
        }
        return null;
    }
}
