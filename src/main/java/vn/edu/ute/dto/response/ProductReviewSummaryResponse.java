package vn.edu.ute.dto.response;

import java.math.BigDecimal;

/**
 * DTO trả về thống kê review của sản phẩm.
 * Không lưu trong bảng products, chỉ tính từ bảng reviews.
 */
public class ProductReviewSummaryResponse {

    private Long productId;
    private BigDecimal averageRating;
    private long reviewCount;

    public ProductReviewSummaryResponse() {
    }

    public ProductReviewSummaryResponse(Long productId, BigDecimal averageRating, long reviewCount) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public Long getProductId() {
        return productId;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }
}