package vn.edu.ute.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.state.OrderContext;
import vn.edu.ute.state.OrderState;

/**
 * Trạng thái Đang Giao Hàng.
 * Khâu này có 2 ngả đường:
 * 1. Giao thành công (-> DELIVERED)
 * 2. Giao thất bại / Bị bùng hàng (-> CANCELLED / RETURN)
 */
public class ShippedState implements OrderState {

    @Override
    public void deliverOrder(OrderContext context) {
        // Finalize order
        context.getOrder().setStatus(OrderStatus.DELIVERED);
        context.setState(new DeliveredState());
        
        System.out.println("[Mock Email Service] Đã trao đến tay khách. Gửi mail Cảm ơn kèm link xin Đánh giá Sản phẩm.");
    }

    @Override
    public void cancelOrder(OrderContext context, String cancelReason) {
        context.getOrder().setStatus(OrderStatus.CANCELLED);
        context.setState(new CancelledState());
        
        System.out.println("[Mock Inventory] Đã hoàn lại sản phẩm vào Database Kho (Restock_ShipReturn).");
        System.out.println("[Mock Info] Shipper báo cáo bom hàng. Lý do: " + cancelReason);
    }
}
