package vn.edu.ute.state;

import vn.edu.ute.entity.Order;
import vn.edu.ute.state.impl.*;

/**
 * OrderContext đóng gói Entity Order và chứa trạng thái hiện tại.
 * Mọi hành động của Admin lên Đơn hàng phải thông qua Context này
 * để đảm bảo State Machine kiểm duyệt tính hợp lệ trước khi chạy.
 */
public class OrderContext {
    private final Order order;
    private OrderState state;

    public OrderContext(Order order) {
        this.order = order;
        // Khởi tạo Trạng thái tương ứng với Status hiện hữu lưu trong Database
        switch (order.getStatus()) {
            case PENDING:
                this.state = new PendingState();
                break;
            case PROCESSING:
                this.state = new ProcessingState();
                break;
            case SHIPPED:
                this.state = new ShippedState();
                break;
            case DELIVERED:
                this.state = new DeliveredState();
                break;
            case CANCELLED:
                this.state = new CancelledState();
                break;
            default:
                throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ!");
        }
    }

    // Dùng cho State class tự động chuyển trạng thái của Context sang bước kế tiếp
    public void setState(OrderState state) {
        this.state = state;
    }

    public Order getOrder() {
        return order;
    }
    
    public OrderState getState() {
        return state;
    }

    // --- CÁC HÀNH ĐỘNG DO ADMIN KÍCH HOẠT (Uỷ quyền cho State xử lý) ---

    public void processOrder() {
        state.processOrder(this);
    }

    public void shipOrder() {
        state.shipOrder(this);
    }

    public void deliverOrder() {
        state.deliverOrder(this);
    }

    public void cancelOrder(String reason) {
        state.cancelOrder(this, reason);
    }
}
