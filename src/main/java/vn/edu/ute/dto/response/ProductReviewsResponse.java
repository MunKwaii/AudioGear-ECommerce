package vn.edu.ute.dto.response;

import java.util.List;

/**
 * DTO gộp thống kê review + danh sách review của sản phẩm.
 */
public class ProductReviewsResponse {

    private ProductReviewSummaryResponse summary;
    private List<ReviewResponse> reviews;

    public ProductReviewsResponse() {
    }

    public ProductReviewsResponse(ProductReviewSummaryResponse summary, List<ReviewResponse> reviews) {
        this.summary = summary;
        this.reviews = reviews;
    }

    public ProductReviewSummaryResponse getSummary() {
        return summary;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }
}