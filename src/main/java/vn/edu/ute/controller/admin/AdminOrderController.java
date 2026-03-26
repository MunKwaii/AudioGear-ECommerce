package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.impl.OrderServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * REST Controller dành cho Admin thao tác chuyển đổi trạng thái Đơn Hàng.
 * Bảo mật: Đã được bảo vệ thông qua WebSecurityConfig ("/api/admin/*" -> requiresAdminRole)
 */
@WebServlet("/api/admin/orders/*")
public class AdminOrderController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, out, 400, "Thiếu hành động (action) trên URL. Ví dụ: /api/admin/orders/process");
            return;
        }

        String orderIdParam = req.getParameter("id");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            sendError(resp, out, 400, "Thiếu tham số ID đơn hàng (id).");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam);
            Order updatedOrder = null;
            String successMessage = "";

            // Phân luồng Action gọi tới OrderService (State Machine tự động được kích hoạt)
            switch (pathInfo) {
                case "/process":
                    updatedOrder = orderService.processOrder(orderId);
                    successMessage = "Duyệt đơn hàng thành công (PROCESSING).";
                    break;
                case "/ship":
                    updatedOrder = orderService.shipOrder(orderId);
                    successMessage = "Đã xuất kho giao cho Đơn vị vận chuyển (SHIPPED).";
                    break;
                case "/deliver":
                    updatedOrder = orderService.deliverOrder(orderId);
                    successMessage = "Hoàn tất đơn hàng (DELIVERED).";
                    break;
                case "/cancel":
                    String reason = req.getParameter("reason");
                    updatedOrder = orderService.cancelOrder(orderId, reason);
                    successMessage = "Huỷ đơn hàng thành công (CANCELLED).";
                    break;
                default:
                    sendError(resp, out, 404, "Hành động không hợp lệ: " + pathInfo);
                    return;
            }

            // Trả về JSON Thành công
            out.println("{");
            out.println("  \"success\": true,");
            out.println("  \"message\": \"" + successMessage + "\",");
            out.println("  \"data\": {");
            out.println("    \"orderId\": " + updatedOrder.getId() + ",");
            out.println("    \"newStatus\": \"" + updatedOrder.getStatus().name() + "\"");
            out.println("  }");
            out.println("}");

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "ID đơn hàng phải là một con số nguyên.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Lỗi quăng ra từ State Machine vì vi phạm tính logic quy trình
            sendError(resp, out, 409, e.getMessage()); // 409 Conflict
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, out, 500, "Lỗi máy chủ: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse resp, PrintWriter out, int statusCode, String message) {
        resp.setStatus(statusCode);
        out.println("{\"success\": false, \"message\": \"" + message + "\"}");
    }
}
