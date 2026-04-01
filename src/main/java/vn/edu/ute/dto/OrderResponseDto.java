package vn.edu.ute.dto;

import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO chuẩn hóa JSON response cho Admin Order API.
 * Tránh expose trực tiếp Entity và tránh string-concat JSON thủ công trong Controller.
 */
public class OrderResponseDto {

    private boolean success;
    private String message;
    private OrderData data;

    // --- Static Factory Methods ---

    public static OrderResponseDto success(String message, Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.success = true;
        dto.message = message;
        dto.data = new OrderData(order);
        return dto;
    }

    public static OrderResponseDto error(String message) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.success = false;
        dto.message = message;
        dto.data = null;
        return dto;
    }

    /**
     * Convert 1 Order entity thành Map<String, Object> để serialize bằng Gson.
     * Dùng trong AdminOrderController.doGet().
     */
    public static Map<String, Object> fromEntity(Order order) {
        return Map.of(
                "orderId", order.getId(),
                "orderCode", order.getOrderCode(),
                "status", order.getStatus().name(),
                "recipientName", order.getRecipientName() != null ? order.getRecipientName() : "",
                "email", order.getEmail() != null ? order.getEmail() : "",
                "totalAmount", order.getTotalAmount(),
                "paymentMethod", order.getPaymentStrategy() != null
                        ? order.getPaymentStrategy().getStrategyCode() : "",
                "createdAt", formatDateTime(order.getCreatedAt()),
                "updatedAt", formatDateTime(order.getUpdatedAt())
        );
    }

    /**
     * Convert danh sách Order entities thành List<Map>.
     * Sử dụng Stream.map() để transform.
     */
    public static List<Map<String, Object>> fromEntities(List<Order> orders) {
        return orders.stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null
                ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "";
    }

    // --- JSON Serialization thủ công (không cần Gson/Jackson) ---

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"success\": ").append(success).append(",\n");
        sb.append("  \"message\": \"").append(escapeJson(message)).append("\"");
        if (data != null) {
            sb.append(",\n  \"data\": ").append(data.toJson());
        }
        sb.append("\n}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // --- Getters ---

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public OrderData getData() { return data; }

    // --- Inner class chứa data của Order ---

    public static class OrderData {
        private final Long orderId;
        private final String orderCode;
        private final OrderStatus status;
        private final String recipientName;
        private final String email;
        private final BigDecimal totalAmount;
        private final String updatedAt;

        public OrderData(Order order) {
            this.orderId = order.getId();
            this.orderCode = order.getOrderCode();
            this.status = order.getStatus();
            this.recipientName = order.getRecipientName();
            this.email = order.getEmail();
            this.totalAmount = order.getTotalAmount();
            this.updatedAt = order.getUpdatedAt() != null
                    ? order.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null;
        }

        public String toJson() {
            return "{\n" +
                    "    \"orderId\": " + orderId + ",\n" +
                    "    \"orderCode\": \"" + escapeJson(orderCode) + "\",\n" +
                    "    \"newStatus\": \"" + status.name() + "\",\n" +
                    "    \"recipientName\": \"" + escapeJson(recipientName) + "\",\n" +
                    "    \"email\": \"" + escapeJson(email) + "\",\n" +
                    "    \"totalAmount\": " + totalAmount + ",\n" +
                    "    \"updatedAt\": \"" + (updatedAt != null ? updatedAt : "") + "\"\n" +
                    "  }";
        }

        private static String escapeJson(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }

        public Long getOrderId() { return orderId; }
        public String getOrderCode() { return orderCode; }
        public OrderStatus getStatus() { return status; }
    }
}
