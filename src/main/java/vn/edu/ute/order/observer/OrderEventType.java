package vn.edu.ute.order.observer;

/**
 * Các loại sự kiện có thể xảy ra trong vòng đời đơn hàng.
 * Subject sẽ broadcast một trong các event này tới tất cả Observer đang lắng nghe.
 */
public enum OrderEventType {
    ORDER_PROCESSING,   // PENDING → PROCESSING (Admin duyệt đơn)
    ORDER_SHIPPED,      // PROCESSING → SHIPPED (Xuất kho)
    ORDER_DELIVERED,    // SHIPPED → DELIVERED (Giao thành công)
    ORDER_CANCELLED     // PENDING/SHIPPED → CANCELLED (Huỷ đơn)
}
