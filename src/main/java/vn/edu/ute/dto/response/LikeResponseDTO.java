package vn.edu.ute.dto.response;

/**
 * DTO trả về kết quả like / unlike review.
 */
public class LikeResponseDTO {

    private boolean liked;
    private long totalLikes;

    public LikeResponseDTO() {
    }

    public LikeResponseDTO(boolean liked, long totalLikes) {
        this.liked = liked;
        this.totalLikes = totalLikes;
    }

    public boolean isLiked() {
        return liked;
    }

    public long getTotalLikes() {
        return totalLikes;
    }
}