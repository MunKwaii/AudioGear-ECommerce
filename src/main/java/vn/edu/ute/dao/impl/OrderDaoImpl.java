package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.entity.Order;

import java.util.List;
import java.util.Optional;

public class OrderDaoImpl implements OrderDao {

    @Override
    public Optional<Order> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Order order = em.find(Order.class, id);
            return Optional.ofNullable(order);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    /**
     * Load Order kèm items và product trong 1 query (JOIN FETCH).
     * Sử dụng khi cần dùng Order.getItems() ngoài transaction
     * (ví dụ: RestockService.restoreStock).
     */
    @Override
    public Optional<Order> findByIdWithItems(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Order order = em.createQuery(
                    "SELECT o FROM Order o " +
                    "JOIN FETCH o.items i " +
                    "JOIN FETCH i.product " +
                    "WHERE o.id = :id",
                    Order.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(order);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Order> findAll() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT o FROM Order o ORDER BY o.createdAt DESC", Order.class)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Order save(Order order) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (order.getId() == null) {
                em.persist(order);
            } else {
                order = em.merge(order);
            }
            DatabaseConfig.commitTransaction();
            return order;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi lưu Order: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
