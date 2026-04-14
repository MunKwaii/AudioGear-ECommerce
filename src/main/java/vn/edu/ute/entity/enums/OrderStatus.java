package vn.edu.ute.entity.enums;

public enum OrderStatus {
    PENDING,    // Chờ xử lý/Chờ thanh toán
    PROCESSING, // Đang xử lý (Đã thanh toán/Xác nhận)
    SHIPPING,   // Đang giao
    DELIVERED,  // Đã giao
    CANCELLED   // Đã hủy
}
