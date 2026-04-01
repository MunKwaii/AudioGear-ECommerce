package vn.edu.ute.order.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.order.state.OrderContext;
import vn.edu.ute.order.state.OrderState;

/**
 * Trạng thái Đang Xử Lý.
 * Tiếp nhận quy trình xuất kho đóng gói.
 * Có 2 đường đi:
 * 1. Tiến lên SHIPPED (xuất kho giao vận)
 * 2. Hủy sang CANCELLED (Admin từ chối sau khi duyệt) + hoàn kho
 */
public class ProcessingState implements OrderState {

    @Override
    public void shipOrder(OrderContext context) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.SHIPPED);

        // 2. Chuyển State Machine sang ShippedState
        context.setState(new ShippedState());

        // 3. Gửi email thông báo đang giao hàng
        context.getNotificationService().notifyShipped(context.getOrder());
    }

    @Override
    public void cancelOrder(OrderContext context, String cancelReason) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.CANCELLED);

        // 2. Chuyển State Machine sang CancelledState (terminal)
        context.setState(new CancelledState());

        // 3. Hoàn trả kho (Admin hủy sau khi đã duyệt → hoàn lại tồn kho)
        context.getRestockService().restoreStock(context.getOrder());

        // 4. Gửi email thông báo hủy + lý do cho khách
        context.getNotificationService().notifyCancelled(context.getOrder(), cancelReason);
    }
}
