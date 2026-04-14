package vn.edu.ute.order.state;

import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderNotificationService;
import vn.edu.ute.service.RestockService;
import vn.edu.ute.service.impl.OrderNotificationServiceImpl;
import vn.edu.ute.service.impl.RestockServiceImpl;
import vn.edu.ute.order.state.impl.*;

/**
 * OrderContext đóng gói Entity Order và chứa trạng thái hiện tại (State Pattern).
 * Mọi hành động của Admin lên Đơn hàng phải thông qua Context này
 * để đảm bảo State Machine kiểm duyệt tính hợp lệ trước khi chạy.
 *
 * Context cũng giữ tham chiếu đến OrderNotificationService và RestockService
 * để các State impl có thể gọi mà không cần tự khởi tạo dependency.
 */
public class OrderContext {

    private final Order order;
    private OrderState state;

    // Services được inject vào Context để các State impl dùng
    private final OrderNotificationService notificationService;
    private final RestockService restockService;

    /**
     * Constructor chính: tự tạo default impl của các service.
     * Dùng cho production code.
     */
    public OrderContext(Order order) {
        this(order, new OrderNotificationServiceImpl(), new RestockServiceImpl());
    }

    /**
     * Constructor có DI đầy đủ: dùng cho Unit Test hoặc khi muốn inject mock.
     */
    public OrderContext(Order order,
                        OrderNotificationService notificationService,
                        RestockService restockService) {
        this.order = order;
        this.notificationService = notificationService;
        this.restockService = restockService;

        // Khởi tạo State tương ứng với Status hiện hữu trong Database
        switch (order.getStatus()) {
            case PENDING:
                this.state = new PendingState();
                break;
            case PROCESSING:
                this.state = new ProcessingState();
                break;
            case SHIPPING:
                this.state = new ShippingState();
                break;
            case DELIVERED:
                this.state = new DeliveredState();
                break;
            case CANCELLED:
                this.state = new CancelledState();
                break;
            default:
                throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + order.getStatus());
        }
    }

    // --- Dùng cho State class tự động chuyển trạng thái ---

    public void setState(OrderState state) {
        this.state = state;
    }

    // --- Getters ---

    public Order getOrder() {
        return order;
    }

    public OrderState getState() {
        return state;
    }

    public OrderNotificationService getNotificationService() {
        return notificationService;
    }

    public RestockService getRestockService() {
        return restockService;
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
