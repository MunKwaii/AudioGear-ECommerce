package vn.edu.ute.order.review.observer;

import vn.edu.ute.entity.Review;

import java.util.List;

/**
 * Publisher dùng để phát sự kiện sau khi review được tạo thành công.
 */
public class ReviewCreatedEventPublisher {

    private final List<ReviewCreatedObserver> observers;

    public ReviewCreatedEventPublisher(List<ReviewCreatedObserver> observers) {
        this.observers = observers;
    }

    /**
     * Phát sự kiện review created đến toàn bộ observer đã đăng ký.
     */
    public void publishReviewCreated(Review review) {
        for (ReviewCreatedObserver observer : observers) {
            observer.onReviewCreated(review);
        }
    }
}