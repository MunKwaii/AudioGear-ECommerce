package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.controller.profile.command.ProfileCommand;
import vn.edu.ute.controller.profile.command.ProfileCommandFactory;
import vn.edu.ute.controller.profile.facade.UserProfileFacade;
import vn.edu.ute.controller.profile.facade.impl.UserProfileFacadeImpl;

import java.io.IOException;

/**
 * UserProfileController: Giờ đây chỉ đóng vai trò là Front Controller cho
 * Module Profile.
 * Sử dụng Command Pattern và Facade Pattern để xử lý logic.
 */
@WebServlet({ "/profile", "/profile/update", "/profile/avatar", "/profile/change-password", "/profile/addresses", "/profile/addresses/add",
        "/profile/addresses/edit", "/profile/addresses/delete", "/profile/addresses/default", "/profile/orders", "/profile/orders/cancel" })
@MultipartConfig
public class UserProfileController extends HttpServlet {

    private UserProfileFacade profileFacade;
    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        // Facade Pattern: Khởi tạo facade điều hướng dịch vụ
        this.profileFacade = new UserProfileFacadeImpl(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine();
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    /**
     * Phương thức tập trung xử lý mọi Request thông qua Command Pattern.
     */
    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String method = req.getMethod();
        Long userId = (Long) req.getAttribute("currentUserId");

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Factory Pattern: Lấy Command phù hợp
        ProfileCommand command = ProfileCommandFactory.getCommand(method, path);

        if (command != null) {
            try {
                // Dependency Injection thủ công (vì không dùng Spring)
                command.init(profileFacade, templateEngine, application);
                command.execute(req, resp, userId);
            } catch (Exception e) {
                req.setAttribute("errorMessage", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
                // Fallback về trang profile nếu có lỗi thực thi
                ProfileCommand fallback = ProfileCommandFactory.getCommand("GET", "/profile");
                fallback.init(profileFacade, templateEngine, application);
                fallback.execute(req, resp, userId);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
