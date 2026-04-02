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
     */
    ProductReviewsResponse getReviewsByProductId(Long productId);

    /**
     * Lấy riêng thống kê review của sản phẩm.
     */
    ProductReviewSummaryResponse getReviewSummaryByProductId(Long productId);
}