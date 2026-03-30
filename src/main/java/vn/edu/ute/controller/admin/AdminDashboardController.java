package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.impl.OrderServiceImpl;

import java.io.IOException;
import java.util.List;

/**
 * Servlet render trang Dashboard quản lý đơn hàng Admin.
 * URL: /admin/orders
 *
 * Bảo mật: tự động được bảo vệ bởi JwtAuthenticationFilter
 * vì path bắt đầu bằng /admin/ (xem WebSecurityConfig.ADMIN_PATHS).
 *
 * Trang HTML sử dụng fetch() JavaScript để gọi REST API (/api/admin/orders/*)
 * mà không cần reload trang → trải nghiệm SPA-like.
 */
@WebServlet("/admin/orders")
public class AdminDashboardController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        // Load danh sách đơn hàng từ DB để server-side render bảng ban đầu
        List<Order> orders;
        try {
            orders = orderService.getAllOrders();
        } catch (Exception e) {
            orders = List.of();
        }

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");

        JakartaServletWebApplication webApp =
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("orders", orders);
        ctx.setVariable("pageTitle", "Quản lý Đơn Hàng");

        engine.process("admin/admin-orders", ctx, resp.getWriter());
    }
}
