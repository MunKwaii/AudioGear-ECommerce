package vn.edu.ute.dto.response;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin một review.
 */
public class ReviewResponse {

    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private long totalLikes;
    private LocalDateTime createdAt;

    public ReviewResponse() {
    }

    public ReviewResponse(Long id,
                          Long productId,
                          Long userId,
                          String userName,
                          Integer rating,
                          String comment,
                          long totalLikes,
                          LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.totalLikes = totalLikes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}