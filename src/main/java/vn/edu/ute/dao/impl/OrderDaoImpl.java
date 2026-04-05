package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;

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
            // LEFT JOIN FETCH: trả về Order kể cả khi không có items
            // (tránh lỗi "Không tìm thấy" khi order không có items trong DB)
            List<Order> results = em.createQuery(
                    "SELECT o FROM Order o " +
                    "LEFT JOIN FETCH o.items i " +
                    "LEFT JOIN FETCH i.product " +
                    "WHERE o.id = :id",
                    Order.class)
                    .setParameter("id", id)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
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
    public List<Order> findAllWithItems() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.items i " +
                    "LEFT JOIN FETCH i.product " +
                    "LEFT JOIN FETCH i.product.brand " +
                    "LEFT JOIN FETCH i.product.category " +
                    "ORDER BY o.createdAt DESC", Order.class)
                    .getResultList();
        } catch (Exception e) {
            return em.createQuery("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items ORDER BY o.createdAt DESC", Order.class)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId ORDER BY o.createdAt DESC", Order.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Order> findByOrderCode(String orderCode) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            List<Order> results = em.createQuery("SELECT o FROM Order o WHERE o.orderCode = :orderCode", Order.class)
                    .setParameter("orderCode", orderCode)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Order> findByOrderCodeWithItems(String orderCode) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            List<Order> results = em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.items i " +
                    "LEFT JOIN FETCH i.product " +
                    "WHERE o.orderCode = :orderCode",
                    Order.class)
                    .setParameter("orderCode", orderCode)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Order> findByOrderCodes(List<String> orderCodes) {
        if (orderCodes == null || orderCodes.isEmpty()) {
            return List.of();
        }
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.items i " +
                    "LEFT JOIN FETCH i.product " +
                    "WHERE o.orderCode IN :orderCodes " +
                    "ORDER BY o.createdAt DESC",
                    Order.class)
                    .setParameter("orderCodes", orderCodes)
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

    @Override
    public void updateStatus(String orderCode, OrderStatus status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            em.createQuery("UPDATE Order o SET o.status = :status, o.updatedAt = CURRENT_TIMESTAMP WHERE o.orderCode = :orderCode")
                    .setParameter("status", status)
                    .setParameter("orderCode", orderCode)
                    .executeUpdate();
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái Order: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
