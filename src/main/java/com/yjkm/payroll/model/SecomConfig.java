package com.yjkm.payroll.model;

import jakarta.persistence.*;

/**
 * 세콤 연동 설정 정보
 * MySQL 데이터베이스 연결 정보를 저장합니다.
 */
@Entity
@Table(name = "secom_config")
public class SecomConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serverAddress;     // MySQL 서버 주소 (예: "localhost", "192.168.0.100")

    private Integer serverPort;       // MySQL 서버 포트 (기본값: 3306)

    private String databaseName;      // 데이터베이스 이름 (기본값: "secomdb")

    private String username;          // MySQL 사용자명

    @Column(length = 500)
    private String password;          // MySQL 비밀번호

    private String tableName;         // 출퇴근 데이터 테이블 이름 (예: "TB_INOUT")

    @Column(length = 1000)
    private String columnMapping;     // 컬럼 매핑 정보 (JSON 형식)

    private Boolean isActive;         // 활성화 여부

    // 기본 생성자
    public SecomConfig() {
        this.serverPort = 3306;
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
     * MySQL 데이터베이스 연결 문자열을 반환합니다.
     */
    public String getJdbcUrl() {
        // 서버 주소를 사용하는 경우 (MySQL)
        if (serverAddress != null && !serverAddress.isEmpty()) {
            StringBuilder url = new StringBuilder("jdbc:mysql://");
            url.append(serverAddress);

            // 포트 지정 (기본값: 3306)
            if (serverPort != null) {
                url.append(":").append(serverPort);
            } else {
                url.append(":3306");
            }

            // 데이터베이스 이름
            if (databaseName != null && !databaseName.isEmpty()) {
                url.append("/").append(databaseName);
            }

            // MySQL 연결 옵션
            url.append("?useSSL=false");
            url.append("&serverTimezone=Asia/Seoul");
            url.append("&characterEncoding=UTF-8");
            url.append("&allowPublicKeyRetrieval=true");

            return url.toString();
        }

        return null;
    }
}
