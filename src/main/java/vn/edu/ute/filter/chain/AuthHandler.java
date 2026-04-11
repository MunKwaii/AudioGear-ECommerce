package vn.edu.ute.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Base Handler cho Chain of Responsibility Pattern.
 */
public abstract class AuthHandler {
    protected AuthHandler next;

    public AuthHandler setNext(AuthHandler next) {
        this.next = next;
        return next;
    }

    public abstract void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    protected void handleNext(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (next != null) {
            next.handle(request, response, chain);
        } else {
            // Nếu qua hết chuỗi mà không bị filter chặn lại, tiếp tục request
            chain.doFilter(request, response);
        }
    }

    protected void sendJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"success\": false, \"message\": \"%s\"}", message));
    }
}
