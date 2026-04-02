package vn.edu.ute.dao;

import vn.edu.ute.entity.ReviewLike;

import java.util.Optional;

/**
 * DAO thao tác với bảng review_likes.
 */
public interface ReviewLikeDao {

    /**
     * Tìm like theo user và review.
     */
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    /**
     * Lưu mới like.
     */
    ReviewLike save(ReviewLike reviewLike);

    /**
     * Xóa like.
     */
    void delete(ReviewLike reviewLike);

    /**
     * Đếm số like của review.
     */
    long countByReviewId(Long reviewId);
}