package vn.edu.ute.order.state;

/**
 * Giao diện đại diện cho một trạng thái của Đơn hàng (State Pattern).
 * Mỗi trạng thái (Pending, Processing...) sẽ implements giao diện này.
 * Bất kỳ hành động chuyển trạng thái nào không hợp lệ sẽ ném ra
 * IllegalStateException.
 */
public interface OrderState {

    /**
     * Chuyển trạng thái từ PENDING -> PROCESSING (Duyệt đơn hàng)
     */
    default void processOrder(OrderContext context) {
        throw new IllegalStateException("Lỗi: Không thể Duyệt/Xác Nhận đơn hàng từ trạng thái hiện tại!");
    }

    /**
     * Chuyển trạng thái từ PROCESSING -> SHIPPED (Giao cho đơn vị vận chuyển)
     */
    default void shipOrder(OrderContext context) {
        throw new IllegalStateException("Lỗi: Không thể Đóng Gói & Giao đơn hàng từ trạng thái hiện tại!");
    }

    /**
     * Chuyển trạng thái từ SHIPPED -> DELIVERED (Giao hàng thành công)
     */
    default void deliverOrder(OrderContext context) {
        throw new IllegalStateException("Lỗi: Không thể Hoàn Tất (Delivered) đơn hàng từ trạng thái hiện tại!");
    }

    /**
     * Chuyển trạng thái sang CANCELLED (Huỷ đơn hàng)
     */
    default void cancelOrder(OrderContext context, String cancelReason) {
        throw new IllegalStateException("Lỗi: Không thể Huỷ đơn hàng từ trạng thái này!");
    }

}
