package com.yjkm.payroll.model;

import jakarta.persistence.*;

/**
 * 세콤 연동 설정 정보
 * ODBC 데이터베이스 연결 정보를 저장합니다.
 */
@Entity
@Table(name = "secom_config")
public class SecomConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dsnName;           // ODBC 데이터 소스 이름 (예: "secomdb")

    private String serverAddress;     // 서버 주소 (필요한 경우)
    private Integer serverPort;       // 서버 포트 (필요한 경우)

    private String databaseName;      // 데이터베이스 이름 (기본값: "secomdb")

    private String username;          // 사용자명 (필요한 경우)

    @Column(length = 500)
    private String password;          // 암호화된 비밀번호 (필요한 경우)

    @Column(nullable = false)
    private String tableName;         // 출퇴근 데이터 테이블 이름 (예: "TB_INOUT")

    @Column(length = 1000)
    private String columnMapping;     // 컬럼 매핑 정보 (JSON 형식)

    private Boolean isActive;         // 활성화 여부

    // 기본 생성자
    public SecomConfig() {
        this.databaseName = "secomdb";
        this.tableName = "TB_INOUT";
        this.isActive = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDsnName() {
        return dsnName;
    }

    public void setDsnName(String dsnName) {
        this.dsnName = dsnName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(String columnMapping) {
        this.columnMapping = columnMapping;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    /**
     * JDBC URL 생성
     * 일반적인 JDBC-ODBC 연결 문자열을 반환합니다.
     */
    public String getJdbcUrl() {
        // ODBC DSN을 사용하는 경우
        if (dsnName != null && !dsnName.isEmpty()) {
            return "jdbc:odbc:" + dsnName;
        }

        // 서버 주소를 사용하는 경우 (MS SQL Server 예시)
        if (serverAddress != null && !serverAddress.isEmpty()) {
            String url = "jdbc:sqlserver://" + serverAddress;
            if (serverPort != null) {
                url += ":" + serverPort;
            }
            if (databaseName != null && !databaseName.isEmpty()) {
                url += ";databaseName=" + databaseName;
            }
            return url;
        }

        return null;
    }
}
