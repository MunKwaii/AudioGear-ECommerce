package vn.edu.ute.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.request.CreateReviewRequest;
import vn.edu.ute.exception.ReviewException;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.service.ReviewLikeService;
import vn.edu.ute.service.ReviewService;
import vn.edu.ute.service.impl.ReviewLikeServiceImpl;
import vn.edu.ute.service.impl.ReviewServiceImpl;
import vn.edu.ute.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Controller xử lý API liên quan đến review:
 * - Tạo mới review
 * - Like / Unlike review
 * - Lấy danh sách review theo product
 * - Lấy thống kê review theo product
 */
@WebServlet(urlPatterns = {"/api/v1/reviews/*"})
public class ReviewController extends HttpServlet {

    private final ReviewService reviewService = new ReviewServiceImpl();
    private final ReviewLikeService reviewLikeService = new ReviewLikeServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String uri = req.getRequestURI();

            // Lấy userId trực tiếp từ attribute mà JwtAuthenticationFilter đã set
            Long userId = (Long) req.getAttribute("currentUserId");

            // API tạo review
            if (uri.endsWith("/api/v1/reviews") || uri.endsWith("/api/v1/reviews/")) {
                String body = readBody(req);
                CreateReviewRequest request = JsonUtil.fromJson(body, CreateReviewRequest.class);

                var result = reviewService.createReview(userId, request);

                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(JsonUtil.toJson(new ApiResponse(true, "Tạo đánh giá thành công", result)));
                return;
            }

            // API like / unlike review
            if (uri.matches(".*/api/v1/reviews/\\d+/like$")) {
                Long reviewId = extractReviewId(uri);

                var result = reviewLikeService.toggleLike(userId, reviewId);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JsonUtil.toJson(new ApiResponse(true, "Thao tác thành công", result)));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, "Endpoint không tồn tại", null)));

        } catch (ReviewException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, e.getMessage(), null)));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null)));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, "Review API chỉ hỗ trợ POST cho reviews và likes qua path này", null)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String uri = req.getRequestURI();

            // Lấy userId trực tiếp từ attribute mà JwtAuthenticationFilter đã set
            Long userId = (Long) req.getAttribute("currentUserId");

            // DELETE /api/v1/reviews/{id}
            if (uri.matches(".*/api/v1/reviews/\\d+$")) {
                Long reviewId = extractReviewId(uri);

                reviewService.deleteReview(userId, reviewId);

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(JsonUtil.toJson(new ApiResponse(true, "Xóa đánh giá thành công", null)));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, "Endpoint không tồn tại", null)));

        } catch (ReviewException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, e.getMessage(), null)));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.toJson(new ApiResponse(false, "Lỗi hệ thống: " + e.getMessage(), null)));
        }
    }

    private Long extractReviewId(String uri) {
        String[] parts = uri.split("/");

        for (int i = 0; i < parts.length; i++) {
            if ("reviews".equals(parts[i]) && i + 1 < parts.length) {
                return Long.parseLong(parts[i + 1]);
            }
        }

        throw new IllegalArgumentException("Không thể lấy reviewId từ URL");
    }

    private String readBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}