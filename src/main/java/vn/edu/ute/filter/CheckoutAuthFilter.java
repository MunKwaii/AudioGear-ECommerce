package vn.edu.ute.filter;

import com.google.gson.Gson;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Filter bảo vệ endpoint checkout — yêu cầu đăng nhập.
 * JwtAuthenticationFilter (chạy trước) đã parse JWT và set currentUserId lên request.
 * Filter này chỉ kiểm tra attribute đó có tồn tại không.
 */
@WebFilter(urlPatterns = {"/api/v1/checkout", "/checkout"})
public class CheckoutAuthFilter implements Filter {

    private final Gson gson = new Gson();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // JwtAuthenticationFilter đã set attribute này khi JWT hợp lệ
        Long currentUserId = (Long) req.getAttribute("currentUserId");

        if (currentUserId == null) {
            if (req.getServletPath().equals("/checkout")) {
                resp.sendRedirect(req.getContextPath() + "/login?redirect=/checkout");
                return;
            }
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Vui lòng đăng nhập để thực hiện checkout"
            )));
            return;
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}