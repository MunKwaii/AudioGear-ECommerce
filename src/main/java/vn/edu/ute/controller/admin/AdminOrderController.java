package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.OrderResponseDto;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.impl.OrderServiceImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * REST Controller dành cho Admin thao tác chuyển đổi trạng thái Đơn Hàng.
 *
 * Bảo mật: "/api/admin/*" được JwtAuthenticationFilter bảo vệ,
 * chỉ User có role ADMIN mới vào được.
 *
 * Endpoints:
 *   GET  /api/admin/orders            → Lấy toàn bộ danh sách đơn hàng
 *   POST /api/admin/orders/process    → PENDING   → PROCESSING
 *   POST /api/admin/orders/ship       → PROCESSING → SHIPPED
 *   POST /api/admin/orders/deliver    → SHIPPED    → DELIVERED
 *   POST /api/admin/orders/cancel     → PENDING/SHIPPED → CANCELLED + Restock
 *
 * Mọi lỗi State Machine (nhảy cóc, lùi trạng thái) trả về HTTP 409 Conflict.
 */
@WebServlet("/api/admin/orders/*")
public class AdminOrderController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();

    // -----------------------------------------------------------------------
    // GET /api/admin/orders  →  Danh sách tất cả đơn hàng
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            List<Order> orders = orderService.getAllOrders();

            // Tuần tự hóa danh sách JSON thủ công
            StringBuilder sb = new StringBuilder();
            sb.append("{\n  \"success\": true,\n  \"count\": ").append(orders.size()).append(",\n  \"data\": [\n");
            for (int i = 0; i < orders.size(); i++) {
                Order o = orders.get(i);
                sb.append("    {\n");
                sb.append("      \"orderId\": ").append(o.getId()).append(",\n");
                sb.append("      \"orderCode\": \"").append(escapeJson(o.getOrderCode())).append("\",\n");
                sb.append("      \"status\": \"").append(o.getStatus().name()).append("\",\n");
                sb.append("      \"recipientName\": \"").append(escapeJson(o.getRecipientName())).append("\",\n");
                sb.append("      \"email\": \"").append(escapeJson(o.getEmail())).append("\",\n");
                sb.append("      \"totalAmount\": ").append(o.getTotalAmount()).append(",\n");
                sb.append("      \"createdAt\": \"").append(o.getCreatedAt() != null ? o.getCreatedAt().toString() : "").append("\"\n");
                sb.append("    }");
                if (i < orders.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("  ]\n}");
            out.print(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, out, 500, "Lỗi máy chủ khi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/admin/orders/{action}?id={orderId}[&reason=...]
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, out, 400,
                    "Thiếu hành động (action) trên URL. Ví dụ: /api/admin/orders/process");
            return;
        }

        String orderIdParam = req.getParameter("id");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            sendError(resp, out, 400, "Thiếu tham số ID đơn hàng (?id=...).");
            return;
        }

        try {
            Long orderId = Long.parseLong(orderIdParam);
            Order updatedOrder;
            String successMessage;

            // State Machine được kích hoạt tại đây thông qua OrderService
            switch (pathInfo) {
                case "/process":
                    updatedOrder   = orderService.processOrder(orderId);
                    successMessage = "Duyệt đơn hàng thành công → PROCESSING.";
                    break;
                case "/ship":
                    updatedOrder   = orderService.shipOrder(orderId);
                    successMessage = "Đã xuất kho giao cho đơn vị vận chuyển → SHIPPED.";
                    break;
                case "/deliver":
                    updatedOrder   = orderService.deliverOrder(orderId);
                    successMessage = "Hoàn tất giao hàng → DELIVERED.";
                    break;
                case "/cancel":
                    String reason  = req.getParameter("reason");
                    updatedOrder   = orderService.cancelOrder(orderId, reason);
                    successMessage = "Huỷ đơn hàng thành công → CANCELLED.";
                    break;
                default:
                    sendError(resp, out, 404, "Hành động không hợp lệ: " + pathInfo);
                    return;
            }

            // Trả về JSON thành công dùng OrderResponseDto
            out.print(OrderResponseDto.success(successMessage, updatedOrder).toJson());

        } catch (NumberFormatException e) {
            sendError(resp, out, 400, "ID đơn hàng phải là số nguyên.");
        } catch (IllegalStateException e) {
            // Lỗi nhảy cóc trạng thái từ State Machine → 409 Conflict
            sendError(resp, out, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            // Không tìm thấy đơn hàng → 404
            sendError(resp, out, 404, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, out, 500, "Lỗi máy chủ: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    private void sendError(HttpServletResponse resp, PrintWriter out,
                           int statusCode, String message) {
        resp.setStatus(statusCode);
        out.print(OrderResponseDto.error(message).toJson());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
