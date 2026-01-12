package com.yjkm.payroll.service;

import com.yjkm.payroll.model.SecomConfig;
import com.yjkm.payroll.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 세콤 설정 관리 서비스
 */
public class SecomConfigService {
    private static final Logger logger = LoggerFactory.getLogger(SecomConfigService.class);

    /**
     * 모든 세콤 설정 조회
     */
    public List<SecomConfig> getAllConfigs() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM SecomConfig s", SecomConfig.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * 활성화된 세콤 설정 조회
     */
    public SecomConfig getActiveConfig() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            List<SecomConfig> configs = em.createQuery(
                            "SELECT s FROM SecomConfig s WHERE s.isActive = true", SecomConfig.class)
                    .getResultList();

            if (configs.isEmpty()) {
                return null;
            }

            // 첫 번째 활성화된 설정 반환
            return configs.get(0);
        } finally {
            em.close();
        }
    }

    /**
     * 세콤 설정 저장
     */
    public SecomConfig saveConfig(SecomConfig config) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // 새로운 설정을 활성화하는 경우, 기존 활성화 설정 비활성화
            if (config.getIsActive()) {
                em.createQuery("UPDATE SecomConfig s SET s.isActive = false WHERE s.isActive = true")
                        .executeUpdate();
            }

            SecomConfig savedConfig;
            if (config.getId() == null) {
                em.persist(config);
                savedConfig = config;
            } else {
                savedConfig = em.merge(config);
            }

            em.getTransaction().commit();
            logger.info("세콤 설정 저장 완료: ID={}", savedConfig.getId());
            return savedConfig;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("세콤 설정 저장 실패", e);
            throw new RuntimeException("세콤 설정 저장 실패: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * 세콤 설정 삭제
     */
    public void deleteConfig(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            SecomConfig config = em.find(SecomConfig.class, id);
            if (config != null) {
                em.remove(config);
                logger.info("세콤 설정 삭제 완료: ID={}", id);
            }
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("세콤 설정 삭제 실패", e);
            throw new RuntimeException("세콤 설정 삭제 실패: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * ID로 세콤 설정 조회
     */
    public SecomConfig getConfigById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(SecomConfig.class, id);
        } finally {
            em.close();
        }
    }
}
