package vn.edu.ute.order.observer;

import vn.edu.ute.entity.Order;

/**
 * Observer Interface (Observer Pattern).
 *
 * Bất kỳ class nào muốn "lắng nghe" sự kiện đơn hàng đều phải implement interface này.
 * Hiện tại: EmailOrderObserver.
 * Tương lai: SmsOrderObserver, PushNotificationObserver, AuditLogObserver...
 */
public interface OrderObserver {
    /**
     * Được gọi bởi Subject (OrderService) khi có sự kiện mới xảy ra.
     *
     * @param order     Đơn hàng vừa được thay đổi trạng thái
     * @param event     Loại sự kiện đã xảy ra
     * @param reason    Lý do (chỉ có giá trị khi event = ORDER_CANCELLED)
     */
    void onOrderEvent(Order order, OrderEventType event, String reason);
}
