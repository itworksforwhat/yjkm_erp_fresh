package com.yjkm.payroll.util;

import com.yjkm.payroll.model.SecomConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 세콤 데이터베이스 연결 유틸리티
 * JDBC를 통해 세콤 데이터베이스에 연결합니다.
 */
public class SecomConnectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(SecomConnectionUtil.class);

    /**
     * 세콤 데이터베이스 연결 생성
     *
     * @param config 세콤 연동 설정
     * @return 데이터베이스 연결 객체
     * @throws SQLException 연결 실패 시
     */
    public static Connection getConnection(SecomConfig config) throws SQLException {
        if (config == null) {
            throw new IllegalArgumentException("세콤 설정이 null입니다.");
        }

        if (!config.getIsActive()) {
            throw new IllegalStateException("세콤 연동이 비활성화 상태입니다.");
        }

        String jdbcUrl = config.getJdbcUrl();
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            throw new IllegalArgumentException("JDBC URL을 생성할 수 없습니다. DSN 또는 서버 주소를 확인하세요.");
        }

        logger.info("세콤 데이터베이스 연결 시도: {}", jdbcUrl);

        Properties props = new Properties();
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            props.setProperty("user", config.getUsername());
        }
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            // TODO: 실제 운영 환경에서는 암호화된 비밀번호를 복호화해야 합니다
            props.setProperty("password", config.getPassword());
        }

        try {
            Connection conn = DriverManager.getConnection(jdbcUrl, props);
            logger.info("세콤 데이터베이스 연결 성공");
            return conn;
        } catch (SQLException e) {
            logger.error("세콤 데이터베이스 연결 실패: {}", e.getMessage());
            throw new SQLException("세콤 데이터베이스 연결 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연결 테스트
     *
     * @param config 세콤 연동 설정
     * @return 연결 성공 여부
     */
    public static boolean testConnection(SecomConfig config) {
        try (Connection conn = getConnection(config)) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.error("연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 연결 안전하게 닫기
     *
     * @param conn 닫을 연결
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                    logger.debug("세콤 데이터베이스 연결 종료");
                }
            } catch (SQLException e) {
                logger.error("연결 종료 중 오류: {}", e.getMessage());
            }
        }
    }
}
