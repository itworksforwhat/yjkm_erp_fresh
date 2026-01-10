package com.yjkm.payroll.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("payrollPU");
            logger.info("✅ Hibernate EntityManagerFactory 초기화 완료");
        } catch (Exception e) {
            logger.error("❌ Hibernate 초기화 실패", e);
            throw new RuntimeException("Hibernate 초기화 실패: " + e.getMessage(), e);
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null) {
            throw new RuntimeException("EntityManagerFactory가 초기화되지 않았습니다");
        }
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            logger.info("✅ Hibernate EntityManagerFactory 종료");
        }
    }
}
