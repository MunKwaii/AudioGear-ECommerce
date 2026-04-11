package vn.edu.ute.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.security.WebSecurityConfig;

import java.io.IOException;

/**
 * Concrete Handler 3: Kiểm tra route công khai (Permit All).
 * Nếu được phép, bỏ qua xác thực và dừng chuỗi tại đây.
 */
public class PermitAllHandler extends AuthHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        if (WebSecurityConfig.isPermitAll(path)) {
            // Cho phép truy cập luôn, không gọi next handler
            chain.doFilter(request, response);
            return;
        }

        // Nếu không phải public route, đi tới vòng kiểm tra kế tiếp
        handleNext(request, response, chain);
    }
}
