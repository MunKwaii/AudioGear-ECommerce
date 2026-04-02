package vn.edu.ute.dto.request;

/**
 * DTO nhận dữ liệu tạo đánh giá từ client.
 */
public class CreateReviewRequest {

    private Long productId;
    private Integer rating;
    private String comment;

    public CreateReviewRequest() {
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}