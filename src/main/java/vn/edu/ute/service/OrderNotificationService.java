package vn.edu.ute.service;

import vn.edu.ute.entity.Order;

/**
 * Interface cho việc gửi thông báo Email tới khách hàng
 * khi trạng thái Đơn hàng thay đổi.
 *
 * Áp dụng Dependency Inversion: StateImpl phụ thuộc vào abstraction này,
 * không phụ thuộc trực tiếp vào Mail library cụ thể.
 * Sau này có thể thay bằng JavaMail / SendGrid mà không ảnh hưởng State layer.
 */
public interface OrderNotificationService {

    /**
     * Gửi Email thông báo đơn hàng đang được xử lý (PENDING -> PROCESSING).
     * @param order Đơn hàng vừa được duyệt
     */
    void notifyProcessing(Order order);

    /**
     * Gửi Email thông báo đơn hàng đang được giao (PROCESSING -> SHIPPED).
     * @param order Đơn hàng vừa được xuất kho
     */
    void notifyShipped(Order order);

    /**
     * Gửi Email cảm ơn sau khi giao hàng thành công (SHIPPED -> DELIVERED).
     * @param order Đơn hàng đã hoàn tất
     */
    void notifyDelivered(Order order);

    /**
     * Gửi Email thông báo huỷ đơn kèm lý do.
     * @param order  Đơn hàng bị huỷ
     * @param reason Lý do huỷ do Admin nhập
     */
    void notifyCancelled(Order order, String reason);
}
