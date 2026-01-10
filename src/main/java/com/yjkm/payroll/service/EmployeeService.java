package com.yjkm.payroll.service;

import com.yjkm.payroll.model.Employee;
import com.yjkm.payroll.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class EmployeeService {

    public void saveEmployee(Employee employee) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (employee.getId() != null && employee.getId() > 0) {
                em.merge(employee);
            } else {
                em.persist(employee);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("직원 저장 실패", e);
        } finally {
            em.close();
        }
    }

    public void deleteEmployee(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Employee emp = em.find(Employee.class, id);
            if (emp != null) {
                em.remove(emp);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("직원 삭제 실패", e);
        } finally {
            em.close();
        }
    }

    public List<Employee> getAllEmployees() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT e FROM Employee e ORDER BY e.employeeNumber", Employee.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Employee getEmployeeById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.find(Employee.class, id);
        } finally {
            em.close();
        }
    }

    public List<Employee> searchEmployees(String name) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Employee e WHERE e.name LIKE :name OR e.department LIKE :name ORDER BY e.employeeNumber",
                    Employee.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
