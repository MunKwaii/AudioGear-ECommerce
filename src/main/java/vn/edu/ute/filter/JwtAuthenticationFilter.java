package vn.edu.ute.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.security.WebSecurityConfig;
import vn.edu.ute.service.RedisService;
import vn.edu.ute.service.impl.RedisServiceImpl;
import vn.edu.ute.util.JwtUtil;

import java.io.IOException;

/**
 * Global Security Filter (Thay thế Spring Security FilterChain).
 * Chặn mọi Request (/*) và áp dụng quy tắc từ WebSecurityConfig.
 * Hoạt động ở chế độ STATELESS (Không lưu Session, chỉ dùng JWT).
 */
@WebFilter("/*")
public class JwtAuthenticationFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public JwtAuthenticationFilter() {
        this.jwtUtil = new JwtUtil();
        this.redisService = new RedisServiceImpl();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String contextPath = httpRequest.getContextPath();
        String requestURI = httpRequest.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // =============== XỬ LÝ TOKEN (LUÔN CHẠY) ===============
        // Mục đích: Để các page công khai (PermitAll) vẫn biết User nào đang đăng nhập (nếu có)
        String authHeader = httpRequest.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null && httpRequest.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : httpRequest.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        boolean hasValidToken = false;
        if (token != null && jwtUtil.validateToken(token) && !redisService.isTokenBlacklisted(token)) {
            hasValidToken = true;
            try {
                String email = jwtUtil.getEmailFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                httpRequest.setAttribute("currentUserEmail", email);
                httpRequest.setAttribute("currentUserId", userId);
                httpRequest.setAttribute("currentUserRole", role);
                
                // Nếu path yêu cầu Admin mà User không phải Admin -> Báo lỗi 403
                if (WebSecurityConfig.requiresAdminRole(path) && !"admin".equalsIgnoreCase(role)) {
                    handleForbidden(path, httpRequest, httpResponse);
                    return;
                }
            } catch (Exception e) {
                logger.error("Lỗi Parsing Token ở Filter", e);
                hasValidToken = false; 
                // Token lỗi -> coi như chưa đăng nhập
            }
        }

        // =============== 1. KIỂM TRA QUYỀN TRUY CẬP (PERMIT ALL) ===============
        if (WebSecurityConfig.isPermitAll(path)) {
            chain.doFilter(request, response);
            return;
        }

        // =============== 2. KIỂM TRA XÁC THỰC BẮT BUỘC ===============
        if (!hasValidToken) {
            handleUnauthorized(path, httpRequest, httpResponse);
            return;
        }

        // 3. Passed mọi chốt chặn -> Tiếp tục Request
        chain.doFilter(request, response);
    }

    /**
     * Chuyển hướng người dùng khi Không có quyền đăng nhập (401)
     */
    private void handleUnauthorized(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.startsWith("/api/")) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để thực hiện chức năng này.");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    /**
     * Xử lý khi Role không đủ quyền (403).
     * - Nếu là /api/* hoặc /admin/* → trả JSON để dễ debug
     * - Nếu là trang HTML khác → redirect về /login kèm thông báo
     */
    private void handleForbidden(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String actualRole = (String) req.getAttribute("currentUserRole");
        String msg = "Truy cập bị từ chối. Role hiện tại: [" + actualRole + "]. Cần role: [admin].";
        logger.warn("403 Forbidden: path={}, role={}", path, actualRole);

        if (path.startsWith("/api/") || path.startsWith("/admin/")) {
            // Trả JSON thay vì redirect → trang /error-403 chưa tồn tại
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, msg);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"success\": false, \"message\": \"%s\"}", message));
    }
}
