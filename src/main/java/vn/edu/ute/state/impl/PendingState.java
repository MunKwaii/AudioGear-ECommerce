package vn.edu.ute.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.state.OrderContext;
import vn.edu.ute.state.OrderState;

/**
 * Trạng thái khởi tạo của đơn hàng.
 * Từ PENDING chỉ có thể sang PROCESSING hoặc huỷ bỏ (CANCELLED).
 */
public class PendingState implements OrderState {

    @Override
    public void processOrder(OrderContext context) {
        // Cập nhật Database Entity
        context.getOrder().setStatus(OrderStatus.PROCESSING);
        
        // Chuyển State Machine sang trạng thái Đang xử lý
        context.setState(new ProcessingState());
        
        // Placeholder gửi Email
        System.out.println("[Mock Email Service] Đã gửi Email chốt đơn đang tiến hành xử lý cho: " + context.getOrder().getEmail());
    }

    @Override
    public void cancelOrder(OrderContext context, String cancelReason) {
        // Cập nhật Database Entity
        context.getOrder().setStatus(OrderStatus.CANCELLED);
        
        // Chuyển State Machine sang trạng thái Huỷ
        context.setState(new CancelledState());
        
        // Placeholder Thống kê, Kho và Email
        System.out.println("[Mock Inventory] Đã cộng lại số lượng vào tồn kho (Restock) vì Admin từ chối đơn.");
        System.out.println("[Mock Email Service] Đã gửi thông báo từ chối đơn hàng cho khách. Lý do: " + cancelReason);
    }
}
