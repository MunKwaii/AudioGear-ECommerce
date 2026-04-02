package vn.edu.ute.order.review.observer;

import vn.edu.ute.entity.Review;

/**
 * Observer interface cho sự kiện review được tạo thành công.
 */
public interface ReviewCreatedObserver {

    /**
     * Được gọi sau khi review mới đã được tạo.
     */
    void onReviewCreated(Review review);
}