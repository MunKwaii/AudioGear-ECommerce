package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.User;

import java.io.IOException;
import java.util.List;

public class ShowOrdersCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        List<Order> orders = facade.getUserOrders(userId);
        User user = facade.getUserDetails(userId);

        WebContext context = createWebContext(req, resp);
        context.setVariable("orders", orders);
        context.setVariable("user", user);
        context.setVariable("activePage", "orders");

         if ("true".equals(req.getParameter("cancelSuccess"))) {
            context.setVariable("successMessage", "Hủy đơn hàng thành công!");
        }

        if (req.getParameter("error") != null && !req.getParameter("error").trim().isEmpty()) {
            context.setVariable("errorMessage", req.getParameter("error"));
        }

        render(req, resp, "orders", context);
    }
}
