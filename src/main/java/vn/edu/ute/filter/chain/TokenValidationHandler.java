package vn.edu.ute.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.service.RedisService;
import vn.edu.ute.util.JwtUtil;

import java.io.IOException;

/**
 * Concrete Handler 1: Giải mã và xác thực Token. 
 * Nếu hợp lệ, gán thông tin User vào Request.
 */
public class TokenValidationHandler extends AuthHandler {
    private static final Logger logger = LogManager.getLogger(TokenValidationHandler.class);
    private final JwtUtil jwtUtil;
    private final RedisService redisService;

    public TokenValidationHandler(JwtUtil jwtUtil, RedisService redisService) {
        this.jwtUtil = jwtUtil;
        this.redisService = redisService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && jwtUtil.validateToken(token) && !redisService.isTokenBlacklisted(token)) {
            try {
                String email = jwtUtil.getEmailFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                request.setAttribute("currentUserEmail", email);
                request.setAttribute("currentUserId", userId);
                request.setAttribute("currentUserRole", role);
                request.setAttribute("hasValidToken", true);
            } catch (Exception e) {
                logger.error("Lỗi Parsing Token ở Filter", e);
                request.setAttribute("hasValidToken", false);
            }
        } else {
            request.setAttribute("hasValidToken", false);
        }

        // Chuyển cho Handler tiếp theo
        handleNext(request, response, chain);
    }
}
