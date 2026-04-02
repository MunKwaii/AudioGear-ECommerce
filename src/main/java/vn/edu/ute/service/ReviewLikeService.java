package vn.edu.ute.service;

import vn.edu.ute.dto.response.LikeResponseDTO;

/**
 * Service xử lý nghiệp vụ like / unlike review.
 */
public interface ReviewLikeService {

    /**
     * Toggle like cho review.
     */
    LikeResponseDTO toggleLike(Long userId, Long reviewId);
}