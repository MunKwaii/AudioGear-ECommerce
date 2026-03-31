package vn.edu.ute.order.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.order.state.OrderContext;
import vn.edu.ute.order.state.OrderState;

/**
 * Trạng thái Đang Xử Lý.
 * Tiếp nhận quy trình xuất kho đóng gói.
 * Chỉ có duy nhất 1 đường tiến là nhảy sang SHIPPED.
 * KHÔNG được phép Huỷ trực tiếp ở giai đoạn này (theo PUML).
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
}
