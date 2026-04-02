package vn.edu.ute.dto.response;

import java.util.List;
import java.util.Map;

/**
 * DTO gộp thống kê review + danh sách review của sản phẩm.
 */
public class ProductReviewsResponse {

    private ProductReviewSummaryResponse summary;
    private Map<Integer, Long> ratingDistribution;
    private List<ReviewResponse> reviews;

    public ProductReviewsResponse() {
    }

    public ProductReviewsResponse(ProductReviewSummaryResponse summary,
                                  Map<Integer, Long> ratingDistribution,
                                  List<ReviewResponse> reviews) {
        this.summary = summary;
        this.ratingDistribution = ratingDistribution;
        this.reviews = reviews;
    }

    public ProductReviewSummaryResponse getSummary() {
        return summary;
    }

    public Map<Integer, Long> getRatingDistribution() {
        return ratingDistribution;
    }

    public List<ReviewResponse> getReviews() {
        return reviews;
    }
}