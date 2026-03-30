package vn.edu.ute.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.state.OrderContext;
import vn.edu.ute.state.OrderState;

/**
 * Trạng thái khởi tạo của đơn hàng.
 * Từ PENDING chỉ có thể sang PROCESSING (duyệt) hoặc CANCELLED (từ chối).
 */
public class PendingState implements OrderState {

    @Override
    public void processOrder(OrderContext context) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.PROCESSING);

        // 2. Chuyển State Machine sang ProcessingState
        context.setState(new ProcessingState());

        // 3. Gửi email thông báo cho khách
        context.getNotificationService().notifyProcessing(context.getOrder());
    }

    @Override
    public void cancelOrder(OrderContext context, String cancelReason) {
        // 1. Cập nhật trạng thái Entity
        context.getOrder().setStatus(OrderStatus.CANCELLED);

        // 2. Chuyển State Machine sang CancelledState
        context.setState(new CancelledState());

        // 3. Hoàn trả số lượng vào kho (PUML: "Cộng lại số lượng vào tồn kho")
        context.getRestockService().restoreStock(context.getOrder());

        // 4. Gửi email thông báo huỷ + lý do cho khách
        context.getNotificationService().notifyCancelled(context.getOrder(), cancelReason);
    }
}
