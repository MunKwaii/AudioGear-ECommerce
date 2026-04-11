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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
 * - Strategy Pattern cho sorting (newest, highest_rating, most_liked)
 */
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;
    private final ReviewLikeDao reviewLikeDao;
    private final ReviewCreatedEventPublisher eventPublisher;

    /**
     * Strategy Pattern: Map tiêu chí sort → Comparator<ReviewResponse>
     */
    private static final Map<String, Comparator<ReviewResponse>> SORT_STRATEGIES = Map.of(
            "newest", Comparator.comparing(ReviewResponse::getCreatedAt).reversed(),
            "highest_rating", Comparator.comparingInt(ReviewResponse::getRating).reversed(),
            "most_liked", Comparator.comparingLong(ReviewResponse::getTotalLikes).reversed()
    );

    public ReviewServiceImpl() {
        this.reviewDao = new ReviewDaoImpl();
        this.reviewLikeDao = new ReviewLikeDaoImpl();

        List<ReviewCreatedObserver> observers = new ArrayList<>();
        observers.add(new ProductReviewStatsObserver(reviewDao));

        this.eventPublisher = new ReviewCreatedEventPublisher(observers);
    }

    @Override
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        try {
            DatabaseConfig.getInstance().beginTransaction();

            if (userId == null) {
                throw new ReviewException("Bạn cần đăng nhập để đánh giá sản phẩm");
            }

            if (request == null) {
                throw new ReviewException("Dữ liệu đánh giá không hợp lệ");
            }

            if (request.getProductId() == null) {
                throw new ReviewException("Thiếu productId");
            }

            if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
                throw new ReviewException("Đánh giá phải từ 1 đến 5 sao");
            }

            if (request.getComment() != null && request.getComment().length() > 1000) {
                throw new ReviewException("Bình luận không được vượt quá 1000 ký tự");
            }

            Product product = em.find(Product.class, request.getProductId());
            if (product == null) {
                throw new ReviewException("Không tìm thấy sản phẩm");
            }

            if (!reviewDao.hasPurchasedAndDelivered(userId, request.getProductId())) {
                throw new ReviewException("Bạn chỉ có thể đánh giá sản phẩm đã mua và đã nhận hàng");
            }

            if (reviewDao.existsByUserIdAndProductId(userId, request.getProductId())) {
                throw new ReviewException("Bạn đã đánh giá sản phẩm này rồi");
            }

            User user = em.getReference(User.class, userId);

            Review review = new Review();
            review.setUser(user);
            review.setProduct(product);
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());

            reviewDao.save(review);
            em.flush();

            eventPublisher.publishReviewCreated(review);

            DatabaseConfig.getInstance().commitTransaction();

            return new ReviewResponse(
                    review.getId(),
                    product.getId(),
                    user.getId(),
                    user.getFullName(),
                    review.getRating(),
                    review.getComment(),
                    0,
                    false,
                    review.getCreatedAt()
            );

        } catch (ReviewException e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new ReviewException("Không thể tạo đánh giá: " + e.getMessage());
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public ProductReviewsResponse getReviewsByProductId(Long productId, Long currentUserId, String sortBy) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        try {
            List<Review> reviews = reviewDao.findByProductId(productId);

            // Stream + Lambda: map Review → ReviewResponse, xác định isLiked cho từng review
            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(review -> {
                        boolean isLiked = currentUserId != null &&
                                reviewLikeDao.findByUserIdAndReviewId(currentUserId, review.getId()).isPresent();
                        return new ReviewResponse(
                                review.getId(),
                                review.getProduct().getId(),
                                review.getUser().getId(),
                                review.getUser().getFullName(),
                                review.getRating(),
                                review.getComment(),
                                reviewLikeDao.countByReviewId(review.getId()),
                                isLiked,
                                review.getCreatedAt()
                        );
                    })
                    .collect(Collectors.toList());

            // Strategy Pattern: sort theo tiêu chí được chọn
            if (sortBy != null && SORT_STRATEGIES.containsKey(sortBy)) {
                reviewResponses.sort(SORT_STRATEGIES.get(sortBy));
            }

            // Tính ratingDistribution bằng Stream groupingBy + counting
            Map<Integer, Long> ratingDistribution = reviews.stream()
                    .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

            ProductReviewSummaryResponse summary = getReviewSummaryByProductId(productId);

            return new ProductReviewsResponse(summary, ratingDistribution, reviewResponses);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public ProductReviewSummaryResponse getReviewSummaryByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

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
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void deleteReview(Long userId, Long reviewId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        try {
            DatabaseConfig.getInstance().beginTransaction();

            if (userId == null) {
                throw new ReviewException("Bạn cần đăng nhập để xóa đánh giá");
            }

            Review review = reviewDao.findById(reviewId)
                    .orElseThrow(() -> new ReviewException("Không tìm thấy đánh giá"));

            if (!review.getUser().getId().equals(userId)) {
                throw new ReviewException("Bạn chỉ có thể xóa đánh giá của mình");
            }

            reviewDao.delete(review);

            DatabaseConfig.getInstance().commitTransaction();

        } catch (ReviewException e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new ReviewException("Không thể xóa đánh giá: " + e.getMessage());
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}