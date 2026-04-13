package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Command xử lý user tự hủy đơn hàng của mình.
 */
public class CancelOrderCommand extends ProfileCommand {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        String orderIdParam = req.getParameter("orderId");

        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            String message = URLEncoder.encode("Thiếu mã đơn hàng cần hủy", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/profile/orders?error=" + message);
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam.trim());
            facade.cancelUserOrder(userId, orderId);
            resp.sendRedirect(req.getContextPath() + "/profile/orders?cancelSuccess=true");
        } catch (NumberFormatException e) {
            String message = URLEncoder.encode("Mã đơn hàng không hợp lệ", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/profile/orders?error=" + message);
        } catch (IllegalArgumentException | IllegalStateException e) {
            String message = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/profile/orders?error=" + message);
        }
    }
}