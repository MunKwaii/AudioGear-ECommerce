package vn.edu.ute.dao;

import vn.edu.ute.entity.Review;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tác với bảng reviews.
 */
public interface ReviewDao {

    /**
     * Lưu mới review.
     */
    Review save(Review review);

    /**
     * Tìm review theo id.
     */
    Optional<Review> findById(Long id);

    /**
     * Kiểm tra user đã review product này chưa.
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Kiểm tra user đã mua product và đơn hàng đã DELIVERED chưa.
     */
    boolean hasPurchasedAndDelivered(Long userId, Long productId);

    /**
     * Lấy danh sách review theo productId.
     */
    List<Review> findByProductId(Long productId);

    /**
     * Tính điểm trung bình review của sản phẩm.
     */
    BigDecimal calculateAverageRatingByProductId(Long productId);

    /**
     * Đếm tổng số review của sản phẩm.
     */
    long countByProductId(Long productId);

    /**
     * Xóa review.
     */
    void delete(Review review);
}