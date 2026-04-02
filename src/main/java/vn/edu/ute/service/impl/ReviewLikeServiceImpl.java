package vn.edu.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ReviewDao;
import vn.edu.ute.dao.ReviewLikeDao;
import vn.edu.ute.dao.impl.ReviewDaoImpl;
import vn.edu.ute.dao.impl.ReviewLikeDaoImpl;
import vn.edu.ute.dto.response.LikeResponseDTO;
import vn.edu.ute.entity.Review;
import vn.edu.ute.entity.ReviewLike;
import vn.edu.ute.entity.User;
import vn.edu.ute.exception.ReviewException;
import vn.edu.ute.service.ReviewLikeService;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service xử lý nghiệp vụ like / unlike review.
 */
public class ReviewLikeServiceImpl implements ReviewLikeService {

    private final ReviewDao reviewDao;
    private final ReviewLikeDao reviewLikeDao;

    public ReviewLikeServiceImpl() {
        this.reviewDao = new ReviewDaoImpl();
        this.reviewLikeDao = new ReviewLikeDaoImpl();
    }

    @Override
    public LikeResponseDTO toggleLike(Long userId, Long reviewId) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            DatabaseConfig.beginTransaction();

            // 1. Kiểm tra đăng nhập
            if (userId == null) {
                throw new ReviewException("Bạn cần đăng nhập để thích đánh giá");
            }

            // 2. Kiểm tra review tồn tại
            Review review = reviewDao.findById(reviewId)
                    .orElseThrow(() -> new ReviewException("Không tìm thấy đánh giá"));

            // 3. Tìm trạng thái like hiện tại
            Optional<ReviewLike> existingLike = reviewLikeDao.findByUserIdAndReviewId(userId, reviewId);

            boolean liked;
            if (existingLike.isPresent()) {
                reviewLikeDao.delete(existingLike.get());
                liked = false;
            } else {
                User user = em.getReference(User.class, userId);

                ReviewLike newLike = new ReviewLike();
                newLike.setUser(user);
                newLike.setReview(review);
                newLike.setCreatedAt(LocalDateTime.now());

                reviewLikeDao.save(newLike);
                liked = true;
            }

            em.flush();
            long totalLikes = reviewLikeDao.countByReviewId(reviewId);

            DatabaseConfig.commitTransaction();

            return new LikeResponseDTO(liked, totalLikes);

        } catch (ReviewException e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new ReviewException("Không thể xử lý like đánh giá: " + e.getMessage());
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}