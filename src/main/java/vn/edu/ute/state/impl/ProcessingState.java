package vn.edu.ute.state.impl;

import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.state.OrderContext;
import vn.edu.ute.state.OrderState;

/**
 * Trạng thái Đang Xử Lý.
 * Tiếp nhận quy trình xuất kho đóng gói.
 * Chỉ có duy nhất 1 đường tiến là nhảy sang SHIPPED (Giao Hàng), KHÔNG được phép Huỷ trực tiếp ở khâu này.
 */
public class ProcessingState implements OrderState {

    @Override
    public void shipOrder(OrderContext context) {
        // Cập nhật Database
        context.getOrder().setStatus(OrderStatus.SHIPPED);
        
        // Chuyển State
        context.setState(new ShippedState());
        
        System.out.println("[Mock Tracking] Đã tạo mã vận chuyển giao hàng.");
        System.out.println("[Mock Email Service] Đã gửi mail thông báo kiện hàng ĐANG TRONG QUÁ TRÌNH GIAO cho khách.");
    }
}
