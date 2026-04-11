package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ReviewDao;
import vn.edu.ute.entity.Review;
import vn.edu.ute.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Triển khai DAO cho Review bằng JPA + EntityManager.
 */
public class ReviewDaoImpl implements ReviewDao {

    @Override
    public Review save(Review review) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        em.persist(review);
        return review;
    }

    @Override
    public Optional<Review> findById(Long id) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        return Optional.ofNullable(em.find(Review.class, id));
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(r) FROM Review r " +
                                "WHERE r.user.id = :userId AND r.product.id = :productId",
                        Long.class
                ).setParameter("userId", userId)
                .setParameter("productId", productId)
                .getSingleResult();

        return count != null && count > 0;
    }

    @Override
    public boolean hasPurchasedAndDelivered(Long userId, Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(oi) " +
                                "FROM OrderItem oi " +
                                "JOIN oi.order o " +
                                "WHERE o.user.id = :userId " +
                                "AND oi.product.id = :productId " +
                                "AND o.status = :status",
                        Long.class
                ).setParameter("userId", userId)
                .setParameter("productId", productId)
                .setParameter("status", OrderStatus.DELIVERED)
                .getSingleResult();

        return count != null && count > 0;
    }

    @Override
    public List<Review> findByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        return em.createQuery(
                        "SELECT r FROM Review r " +
                                "JOIN FETCH r.user u " +
                                "JOIN FETCH r.product p " +
                                "WHERE p.id = :productId " +
                                "ORDER BY r.createdAt DESC",
                        Review.class
                ).setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public BigDecimal calculateAverageRatingByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        Double avg = em.createQuery(
                        "SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId",
                        Double.class
                ).setParameter("productId", productId)
                .getSingleResult();

        if (avg == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public long countByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId",
                        Long.class
                ).setParameter("productId", productId)
                .getSingleResult();

        return count == null ? 0 : count;
    }

    @Override
    public void delete(Review review) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        Review managed = em.contains(review) ? review : em.merge(review);
        em.remove(managed);
    }
}