package vn.edu.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ReviewDao;
import vn.edu.ute.dao.ReviewLikeDao;
import vn.edu.ute.dao.impl.ReviewDaoImpl;
import vn.edu.ute.dao.impl.ReviewLikeDaoImpl;
import vn.edu.ute.dto.request.CreateReviewRequest;
import vn.edu.ute.dto.response.ProductReviewSummaryResponse;
import vn.edu.ute.dto.response.ProductReviewsResponse;
import vn.edu.ute.dto.response.ReviewResponse;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.Review;
import vn.edu.ute.entity.User;
import vn.edu.ute.exception.ReviewException;
import vn.edu.ute.order.review.observer.ReviewCreatedEventPublisher;
import vn.edu.ute.order.review.observer.ReviewCreatedObserver;
import vn.edu.ute.order.review.observer.ProductReviewStatsObserver;
import vn.edu.ute.service.ReviewService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service triển khai nghiệp vụ đánh giá sản phẩm.
 *
 * Chức năng:
 * - Validate dữ liệu đầu vào
 * - Kiểm tra điều kiện đã mua và đã nhận hàng
 * - Kiểm tra user đã review sản phẩm này chưa
 * - Lưu review mới
 * - Phát sự kiện review created bằng Observer Pattern
 */
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final ReviewLikeDao reviewLikeDao;
    private final ReviewCreatedEventPublisher eventPublisher;

    public ReviewServiceImpl() {
        this.reviewDao = new ReviewDaoImpl();
        this.reviewLikeDao = new ReviewLikeDaoImpl();

        List<ReviewCreatedObserver> observers = new ArrayList<>();
        observers.add(new ProductReviewStatsObserver(reviewDao));

        this.eventPublisher = new ReviewCreatedEventPublisher(observers);
    }

    @Override
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            DatabaseConfig.beginTransaction();

            // 1. Kiểm tra đăng nhập
            if (userId == null) {
                throw new ReviewException("Bạn cần đăng nhập để đánh giá sản phẩm");
            }

            // 2. Kiểm tra request
            if (request == null) {
                throw new ReviewException("Dữ liệu đánh giá không hợp lệ");
            }

            // 3. Kiểm tra productId
            if (request.getProductId() == null) {
                throw new ReviewException("Thiếu productId");
            }

            // 4. Kiểm tra rating
            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                throw new ReviewException("Đánh giá phải từ 1 đến 5 sao");
            }

            // 5. Kiểm tra comment
            if (request.getComment() != null && request.getComment().length() > 1000) {
                throw new ReviewException("Bình luận không được vượt quá 1000 ký tự");
            }

            // 6. Kiểm tra product tồn tại
            Product product = em.find(Product.class, request.getProductId());
            if (product == null) {
                throw new ReviewException("Không tìm thấy sản phẩm");
            }

            // 7. Kiểm tra đã mua và đơn đã DELIVERED chưa
            if (!reviewDao.hasPurchasedAndDelivered(userId, request.getProductId())) {
                throw new ReviewException("Bạn chỉ có thể đánh giá sản phẩm đã mua và đã nhận hàng");
            }

            // 8. Kiểm tra đã review chưa
            if (reviewDao.existsByUserIdAndProductId(userId, request.getProductId())) {
                throw new ReviewException("Bạn đã đánh giá sản phẩm này rồi");
            }

            // 9. Lấy user hiện tại
            User user = em.getReference(User.class, userId);

            // 10. Tạo review mới
            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());

            // 11. Lưu review
            reviewDao.save(review);
            em.flush();

            // 12. Phát sự kiện review created
            eventPublisher.publishReviewCreated(review);

            DatabaseConfig.commitTransaction();

            return new ReviewResponse(
                    review.getId(),
                    product.getId(),
                    user.getId(),
                    user.getFullName(),
                    review.getRating(),
                    review.getComment(),
                    0,
                    review.getCreatedAt()
            );

        } catch (ReviewException e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new ReviewException("Không thể tạo đánh giá: " + e.getMessage());
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public ProductReviewsResponse getReviewsByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            List<Review> reviews = reviewDao.findByProductId(productId);

            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(review -> new ReviewResponse(
                            review.getId(),
                            review.getProduct().getId(),
                            review.getUser().getId(),
                            review.getUser().getFullName(),
                            review.getRating(),
                            review.getComment(),
                            reviewLikeDao.countByReviewId(review.getId()),
                            review.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            ProductReviewSummaryResponse summary = getReviewSummaryByProductId(productId);

            return new ProductReviewsResponse(summary, reviewResponses);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public ProductReviewSummaryResponse getReviewSummaryByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            Product product = em.find(Product.class, productId);
            if (product == null) {
                throw new ReviewException("Không tìm thấy sản phẩm");
            }

            return new ProductReviewSummaryResponse(
                    productId,
                    reviewDao.calculateAverageRatingByProductId(productId),
                    reviewDao.countByProductId(productId)
            );
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}