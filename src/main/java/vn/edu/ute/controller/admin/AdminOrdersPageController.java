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
 * Trang quản lý Đơn hàng (Admin UI)
 * URL: /admin/orders
 */
@WebServlet("/admin/orders")
public class AdminOrdersPageController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

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
        ctx.setVariable("title", "Quản lý Đơn hàng");
        ctx.setVariable("activePage", "orders");

        engine.process("admin/admin-orders", ctx, resp.getWriter());
    }
}
