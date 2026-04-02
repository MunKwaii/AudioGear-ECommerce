package vn.edu.ute.service;

import vn.edu.ute.dto.request.CreateReviewRequest;
import vn.edu.ute.dto.response.ProductReviewSummaryResponse;
import vn.edu.ute.dto.response.ProductReviewsResponse;
import vn.edu.ute.dto.response.ReviewResponse;

/**
 * Service xử lý nghiệp vụ review sản phẩm.
 */
public interface ReviewService {

    /**
     * Tạo mới review cho sản phẩm.
     */
    ReviewResponse createReview(Long userId, CreateReviewRequest request);

    /**
     * Lấy toàn bộ review + thống kê review của sản phẩm.
     * @param currentUserId ID của user hiện tại (có thể null nếu chưa đăng nhập)
     * @param sortBy        Tiêu chí sắp xếp: "newest", "highest_rating", "most_liked"
     */
    ProductReviewsResponse getReviewsByProductId(Long productId, Long currentUserId, String sortBy);

    /**
     * Lấy riêng thống kê review của sản phẩm.
     */
    ProductReviewSummaryResponse getReviewSummaryByProductId(Long productId);

    /**
     * Xóa review của user.
     */
    void deleteReview(Long userId, Long reviewId);
}