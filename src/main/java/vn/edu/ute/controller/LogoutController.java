package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.service.RedisService;
import vn.edu.ute.util.JwtUtil;

import java.io.IOException;
import java.util.Date;

@WebServlet("/api/auth/logout")
public class LogoutController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(LogoutController.class);
    private final JwtUtil jwtUtil = new JwtUtil();
    private final RedisService redisService = new RedisService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. Tìm Access Token để gắn vào Blacklist (nếu có)
        String authHeader = req.getHeader("Authorization");
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        if (token == null && req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            try {
                // Tính toán thời gian sống còn lại của Token để lưu vào Redis
                Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
                if (expirationDate != null) {
                    long now = System.currentTimeMillis();
                    long ttlMillis = expirationDate.getTime() - now;
                    if (ttlMillis > 0) {
                        redisService.blacklistAccessToken(token, ttlMillis / 1000); // Lưu theo giây
                        logger.info("Token đã được đưa vào Blacklist thành công.");
                    }
                }
            } catch (Exception e) {
                logger.warn("Không thể parse Token để Blacklist khi Logout: " + e.getMessage());
            }
        }

        // 2. Xoá Cookie accessToken ở phía Trình duyệt
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Bằng 0 nghĩa là ra lệnh cho trình duyệt Xoá ngay Cookie
        resp.addCookie(cookie);

        // 3. Huỷ Session cũ (nếu có dùng chung)
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 4. Chuyển hướng người dùng về trang Đăng nhập
        resp.sendRedirect(req.getContextPath() + "/login?logout=success");
    }
}
