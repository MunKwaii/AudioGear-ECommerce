package vn.edu.ute.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Filter cho phép cả guest và user đã đăng nhập truy cập checkout.
 * JwtAuthenticationFilter (chạy trước) đã parse JWT và set currentUserId lên request nếu có.
 * Guest (currentUserId == null) được phép tiếp tục checkout.
 */
@WebFilter(urlPatterns = {"/api/v1/checkout", "/checkout"})
public class CheckoutAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Cho phép cả guest (currentUserId == null) và user đã đăng nhập
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}