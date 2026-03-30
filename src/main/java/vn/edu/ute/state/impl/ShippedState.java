package vn.edu.ute.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.state.OrderContext;
import vn.edu.ute.state.OrderState;

/**
 * Trạng thái Đang Giao Hàng.
 * Khâu này có 2 ngả đường theo PUML:
 * 1. Giao thành công  -> DELIVERED
 * 2. Giao thất bại / Khách từ chối nhận hàng -> CANCELLED + Restock
 */
public class ShippedState implements OrderState {

    @Override
    public void deliverOrder(OrderContext context) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.DELIVERED);

        // 2. Chuyển State Machine sang DeliveredState (terminal)
        context.setState(new DeliveredState());

        // 3. Gửi email cảm ơn + link đánh giá
        context.getNotificationService().notifyDelivered(context.getOrder());
    }

    @Override
    public void cancelOrder(OrderContext context, String cancelReason) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.CANCELLED);

        // 2. Chuyển State Machine sang CancelledState (terminal)
        context.setState(new CancelledState());

        // 3. Hoàn trả kho (Restock - shipper báo bùng/từ chối)
        context.getRestockService().restoreStock(context.getOrder());

        // 4. Gửi email thông báo huỷ
        context.getNotificationService().notifyCancelled(context.getOrder(), cancelReason);
    }
}
