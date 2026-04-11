package vn.edu.ute.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.filter.chain.*;
import vn.edu.ute.service.RedisService;
import vn.edu.ute.service.impl.RedisServiceImpl;
import vn.edu.ute.util.JwtUtil;

import java.io.IOException;

/**
 * Global Security Filter.
 * Sử dụng bộ khung: Chain of Responsibility Pattern.
 */
@WebFilter("/*")
public class JwtAuthenticationFilter implements Filter {

    private AuthHandler headHandler;

    public JwtAuthenticationFilter() {
        JwtUtil jwtUtil = new JwtUtil();
        RedisService redisService = new RedisServiceImpl();

        // 1. Khởi tạo các Handler độc lập
        AuthHandler tokenValidationHandler = new TokenValidationHandler(jwtUtil, redisService);
        AuthHandler adminRoleHandler = new AdminRoleHandler();
        AuthHandler permitAllHandler = new PermitAllHandler();
        AuthHandler authCheckHandler = new AuthenticationCheckHandler();

        // 2. Build Chain of Responsibility Pattern
        // Các mốc kiểm tra độc lập và nối tiếp nhau đúng chuẩn lý thuyết:
        // Cùng thực hiện việc chung (Filter Path) như trong Video hướng dẫn
        // B1: Phân tích Token và phân quyền (Token Validation)
        // B2: Xác nhận nếu là trang Admin (Chi tiết ngặt nghèo nhất)
        // B3: Xác nhận nếu là trang công khai (Permit All => được cho qua luôn)
        // B4: Không thuộc những trường hợp trên -> Tức là vào trang có tính bảo mật nhưng ko login -> Từ chối
        tokenValidationHandler
                .setNext(adminRoleHandler)
                .setNext(permitAllHandler)
                .setNext(authCheckHandler);

        this.headHandler = tokenValidationHandler;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Kích hoạt chuỗi xử lý Filter
        headHandler.handle(httpRequest, httpResponse, chain);
    }
}
