package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ReviewLikeDao;
import vn.edu.ute.entity.ReviewLike;

import java.util.List;
import java.util.Optional;

/**
 * Triển khai DAO cho ReviewLike bằng JPA + EntityManager.
 */
public class ReviewLikeDaoImpl implements ReviewLikeDao {

    @Override
    public Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        List<ReviewLike> results = em.createQuery(
                        "SELECT rl FROM ReviewLike rl " +
                                "WHERE rl.user.id = :userId AND rl.review.id = :reviewId",
                        ReviewLike.class
                ).setParameter("userId", userId)
                .setParameter("reviewId", reviewId)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public ReviewLike save(ReviewLike reviewLike) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        em.persist(reviewLike);
        return reviewLike;
    }

    @Override
    public void delete(ReviewLike reviewLike) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        ReviewLike managed = em.contains(reviewLike) ? reviewLike : em.merge(reviewLike);
        em.remove(managed);
    }

    @Override
    public long countByReviewId(Long reviewId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        Long count = em.createQuery(
                        "SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.review.id = :reviewId",
                        Long.class
                ).setParameter("reviewId", reviewId)
                .getSingleResult();

        return count == null ? 0 : count;
    }
}