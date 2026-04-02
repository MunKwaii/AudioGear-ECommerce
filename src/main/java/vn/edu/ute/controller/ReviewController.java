package vn.edu.ute.controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.request.CreateReviewRequest;
import vn.edu.ute.exception.ReviewException;
import vn.edu.ute.security.CurrentUser;
import vn.edu.ute.security.JwtUserParser;
import vn.edu.ute.service.ReviewLikeService;
import vn.edu.ute.service.ReviewService;
import vn.edu.ute.service.impl.ReviewLikeServiceImpl;
import vn.edu.ute.service.impl.ReviewServiceImpl;

import java.io.IOException;
import java.util.Map;

/**
 * Controller xử lý API liên quan đến review:
 * - Tạo mới review
 * - Like / Unlike review
 * - Lấy danh sách review theo product
 * - Lấy thống kê review theo product
 */
@WebServlet(urlPatterns = {"/api/v1/reviews/*"})
public class ReviewController extends HttpServlet {

    private final Gson gson = new Gson();
    private final JwtUserParser jwtUserParser = new JwtUserParser();
    private final ReviewService reviewService = new ReviewServiceImpl();
    private final ReviewLikeService reviewLikeService = new ReviewLikeServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String uri = req.getRequestURI();

            CurrentUser currentUser = jwtUserParser.parseFromRequest(req);
            Long userId = currentUser != null ? currentUser.getUserId() : null;

            // API tạo review
            if (uri.endsWith("/api/v1/reviews") || uri.endsWith("/api/v1/reviews/")) {
                CreateReviewRequest request = gson.fromJson(req.getReader(), CreateReviewRequest.class);

                var result = reviewService.createReview(userId, request);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(gson.toJson(result));
                return;
            }

            // API like / unlike review
            if (uri.matches(".*/api/v1/reviews/\\d+/like$")) {
                Long reviewId = extractReviewId(uri);

                var result = reviewLikeService.toggleLike(userId, reviewId);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(result));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(gson.toJson(Map.of("message", "Endpoint không tồn tại")));

        } catch (ReviewException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of("message", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("message", "Lỗi hệ thống")));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(Map.of("message", "Review API chỉ hỗ trợ POST cho reviews và likes qua path này")));
    }

    /**
     * Tách reviewId từ URL dạng /api/v1/reviews/{reviewId}/like
     */
    private Long extractReviewId(String uri) {
        String[] parts = uri.split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("reviews".equals(parts[i]) && i + 1 < parts.length) {
                return Long.parseLong(parts[i + 1]);
            }
        }

        throw new IllegalArgumentException("Không thể lấy reviewId từ URL");
    }


}