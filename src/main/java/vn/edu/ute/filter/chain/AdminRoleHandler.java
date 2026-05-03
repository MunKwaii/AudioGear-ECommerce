package vn.edu.ute.filter.chain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.security.WebSecurityConfig;

import java.io.IOException;

/**
 * Concrete Handler 2: Kiểm tra truy cập quyền Admin.
 * Nếu route yêu cầu Admin mà người dùng không có, chặn luôn (403).
 */
public class AdminRoleHandler extends AuthHandler {
    private static final Logger logger = LogManager.getLogger(AdminRoleHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        Boolean hasValidToken = (Boolean) request.getAttribute("hasValidToken");

        if (Boolean.TRUE.equals(hasValidToken) && WebSecurityConfig.requiresAdminRole(path)) {
            String role = (String) request.getAttribute("currentUserRole");
            if (!"admin".equalsIgnoreCase(role)) {
                handleForbidden(path, role, request, response);
                return; // Chặn yêu cầu tại đây
            }
        }

        // Không vi phạm -> tới Handler kế tiếp
        handleNext(request, response, chain);
    }

    private void handleForbidden(String path, String actualRole, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String msg = "Truy cập bị từ chối. Role hiện tại: [" + actualRole + "]. Cần role: [admin].";
        logger.warn("403 Forbidden: path={}, role={}", path, actualRole);

        if (path.startsWith("/api/") || path.startsWith("/admin/")) {
            sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, msg);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }
}
