package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.config.ThymeleafConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Controller chuyên điều hướng hiển thị trang Đăng Nhập
 */
@WebServlet("/login")
public class LoginController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderPage(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usernameOrEmail = req.getParameter("usernameOrEmail");
        String password = req.getParameter("password");

        try {
            // Khởi tạo AuthService và gọi LoginStrategy Local
            vn.edu.ute.service.AuthService authService = new vn.edu.ute.service.impl.AuthServiceImpl();
            vn.edu.ute.dto.request.AuthRequest authReq = new vn.edu.ute.dto.request.AuthRequest();
            authReq.setUsernameOrEmail(usernameOrEmail);
            authReq.setPassword(password);

            vn.edu.ute.entity.User user = authService.login(vn.edu.ute.auth.LoginType.LOCAL, authReq);

            // Tạo Token JWT
            vn.edu.ute.util.JwtUtil jwtUtil = new vn.edu.ute.util.JwtUtil();
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

            // Lưu JWT vào Cookie
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("accessToken", token);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 ngày
            resp.addCookie(cookie);

            // Merge guest cart if exists
            mergeGuestCartIfPresent(req, resp, user.getId());

            // Chuyển hướng
            if (vn.edu.ute.entity.enums.UserRole.admin.equals(user.getRole())) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/");
            }

        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            renderPage(req, resp);
        }
    }

    private void renderPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        ITemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        if (req.getAttribute("errorMessage") != null) {
            context.setVariable("errorMessage", req.getAttribute("errorMessage"));
        }

        templateEngine.process("login", context, resp.getWriter());
    }

    private void mergeGuestCartIfPresent(HttpServletRequest req, HttpServletResponse resp, Long userId) {
        if (req.getCookies() == null) return;

        Arrays.stream(req.getCookies())
                .filter(c -> "guest_cart".equals(c.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    try {
                        List<vn.edu.ute.dto.request.CheckoutItemRequest> guestItems =
                                vn.edu.ute.cart.GuestCartService.getInstance().toCheckoutItems(req);
                        vn.edu.ute.cart.CartFacadeService cartFacade = vn.edu.ute.cart.CartFacadeServiceImpl.getInstance();
                        cartFacade.mergeCart(userId, guestItems);
                        vn.edu.ute.cart.GuestCartService.getInstance().clearCart(resp);
                    } catch (Exception e) {
                        // Silently fail merge - don't break login
                    }
                });
    }
}
