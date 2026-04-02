package vn.edu.ute.order.review.observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.dao.ReviewDao;
import vn.edu.ute.entity.Review;
import vn.edu.ute.order.review.observer.ReviewCreatedObserver;

import java.math.BigDecimal;

/**
 * Observer dùng để tính lại thống kê review của sản phẩm
 * sau khi có review mới được tạo thành công.
 */
public class ProductReviewStatsObserver implements ReviewCreatedObserver {

    private static final Logger logger = LogManager.getLogger(ProductReviewStatsObserver.class);

    private final ReviewDao reviewDao;

    public ProductReviewStatsObserver(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    @Override
    public void onReviewCreated(Review review) {
        Long productId = review.getProduct().getId();

        BigDecimal averageRating = reviewDao.calculateAverageRatingByProductId(productId);
        long reviewCount = reviewDao.countByProductId(productId);

        logger.info(
                "Review stats recalculated for productId={}, averageRating={}, reviewCount={}",
                productId, averageRating, reviewCount
        );

        // Sau này nếu nhóm cần:
        // - invalidate cache
        // - gửi notification
        // - ghi audit log
        // có thể mở rộng ngay tại đây mà không sửa ReviewService.
    }
}