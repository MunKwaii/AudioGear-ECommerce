package vn.edu.ute.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Concrete Handler 4: Kiểm tra trạng thái xác thực bắt buộc.
 * Bất kì Request nào qua tới bước này đều yêu cầu đăng nhập.
 */
public class AuthenticationCheckHandler extends AuthHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        Boolean hasValidToken = (Boolean) request.getAttribute("hasValidToken");

        if (hasValidToken == null || !hasValidToken) {
            handleUnauthorized(path, request, response);
            return; // Chưa đăng nhập -> chặn
        }

        // Done (End of chain)
        handleNext(request, response, chain);
    }

    private void handleUnauthorized(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.startsWith("/api/")) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để thực hiện chức năng này.");
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }
}
