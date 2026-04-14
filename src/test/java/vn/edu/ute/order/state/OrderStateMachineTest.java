package vn.edu.ute.order.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.order.state.OrderContext;
import vn.edu.ute.service.OrderNotificationService;
import vn.edu.ute.service.RestockService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test cho Order State Machine.
 *
 * KHÔNG cần Database, KHÔNG cần Tomcat.
 * Dùng Mock (lambda) thay cho NotificationService và RestockService thực.
 *
 * Chạy bằng: mvn test -Dtest=OrderStateMachineTest
 */
@DisplayName("Order State Machine Tests")
class OrderStateMachineTest {

    // Mock services: không làm gì thực sự (no-op)
    private OrderNotificationService mockNotification;
    private RestockService mockRestock;

    @BeforeEach
    void setUp() {
        mockNotification = new OrderNotificationService() {
            @Override public void notifyProcessing(Order order) {}
            @Override public void notifyShipping(Order order)   {}
            @Override public void notifyDelivered(Order order)  {}
            @Override public void notifyCancelled(Order order, String reason) {}
        };
        mockRestock = order -> {}; // RestockService là functional interface
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------
    private Order buildOrder(OrderStatus status) {
        Order o = new Order();
        o.setStatus(status);
        o.setEmail("test@test.com");
        o.setOrderCode("ORD-TEST-001");
        o.setRecipientName("Nguyễn Văn A");
        o.setPhoneNumber("0909000000");
        o.setStreetAddress("123 Đường ABC");
        o.setCity("TP.HCM");
        return o;
    }

    private OrderContext ctx(OrderStatus status) {
        return new OrderContext(buildOrder(status), mockNotification, mockRestock);
    }

    // =======================================================================
    // HAPPY PATH: Luồng hợp lệ
    // =======================================================================

    @Test
    @DisplayName("✅ PENDING → PROCESSING")
    void testPendingToProcessing() {
        OrderContext ctx = ctx(OrderStatus.PENDING);
        ctx.processOrder();
        assertEquals(OrderStatus.PROCESSING, ctx.getOrder().getStatus());
        assertInstanceOf(vn.edu.ute.order.state.impl.ProcessingState.class, ctx.getState());
    }

    @Test
    @DisplayName("✅ PROCESSING → SHIPPING")
    void testProcessingToShipping() {
        OrderContext ctx = ctx(OrderStatus.PROCESSING);
        ctx.shipOrder();
        assertEquals(OrderStatus.SHIPPING, ctx.getOrder().getStatus());
        assertInstanceOf(vn.edu.ute.order.state.impl.ShippingState.class, ctx.getState());
    }

    @Test
    @DisplayName("✅ SHIPPING → DELIVERED")
    void testShippingToDelivered() {
        OrderContext ctx = ctx(OrderStatus.SHIPPING);
        ctx.deliverOrder();
        assertEquals(OrderStatus.DELIVERED, ctx.getOrder().getStatus());
        assertInstanceOf(vn.edu.ute.order.state.impl.DeliveredState.class, ctx.getState());
    }

    @Test
    @DisplayName("✅ Full happy path: PENDING → PROCESSING → SHIPPING → DELIVERED")
    void testFullHappyPath() {
        OrderContext ctx = ctx(OrderStatus.PENDING);

        ctx.processOrder();
        assertEquals(OrderStatus.PROCESSING, ctx.getOrder().getStatus());

        ctx.shipOrder();
        assertEquals(OrderStatus.SHIPPING, ctx.getOrder().getStatus());

        ctx.deliverOrder();
        assertEquals(OrderStatus.DELIVERED, ctx.getOrder().getStatus());
    }

    // =======================================================================
    // CANCEL: Huỷ đơn ở các giai đoạn hợp lệ
    // =======================================================================

    @Test
    @DisplayName("✅ PENDING → CANCELLED (Admin từ chối)")
    void testCancelFromPending() {
        OrderContext ctx = ctx(OrderStatus.PENDING);
        ctx.cancelOrder("Không đủ hàng");
        assertEquals(OrderStatus.CANCELLED, ctx.getOrder().getStatus());
        assertInstanceOf(vn.edu.ute.order.state.impl.CancelledState.class, ctx.getState());
    }

    @Test
    @DisplayName("✅ SHIPPING → CANCELLED (Giao thất bại)")
    void testCancelFromShipping() {
        OrderContext ctx = ctx(OrderStatus.SHIPPING);
        ctx.cancelOrder("Khách từ chối nhận hàng");
        assertEquals(OrderStatus.CANCELLED, ctx.getOrder().getStatus());
        assertInstanceOf(vn.edu.ute.order.state.impl.CancelledState.class, ctx.getState());
    }

    // =======================================================================
    // INVALID TRANSITIONS: State Machine phải ném IllegalStateException
    // =======================================================================

    @Test
    @DisplayName("❌ PENDING không thể SHIP (nhảy cóc)")
    void testPendingCannotShip() {
        OrderContext ctx = ctx(OrderStatus.PENDING);
        assertThrows(IllegalStateException.class, ctx::shipOrder);
    }

    @Test
    @DisplayName("❌ PENDING không thể DELIVER (nhảy cóc 2 bước)")
    void testPendingCannotDeliver() {
        OrderContext ctx = ctx(OrderStatus.PENDING);
        assertThrows(IllegalStateException.class, ctx::deliverOrder);
    }

    @Test
    @DisplayName("❌ PROCESSING không thể CANCEL (theo PUML)")
    void testProcessingCannotCancel() {
        OrderContext ctx = ctx(OrderStatus.PROCESSING);
        assertThrows(IllegalStateException.class, () -> ctx.cancelOrder("Thử xem được không"));
    }

    @Test
    @DisplayName("❌ PROCESSING không thể DELIVER (nhảy cóc)")
    void testProcessingCannotDeliver() {
        OrderContext ctx = ctx(OrderStatus.PROCESSING);
        assertThrows(IllegalStateException.class, ctx::deliverOrder);
    }

    @Test
    @DisplayName("❌ DELIVERED không thể bị CANCEL (terminal state)")
    void testDeliveredCannotCancel() {
        OrderContext ctx = ctx(OrderStatus.DELIVERED);
        assertThrows(IllegalStateException.class, () -> ctx.cancelOrder("Muốn lùi lại"));
    }

    @Test
    @DisplayName("❌ DELIVERED không thể PROCESS lại (terminal state)")
    void testDeliveredCannotProcess() {
        OrderContext ctx = ctx(OrderStatus.DELIVERED);
        assertThrows(IllegalStateException.class, ctx::processOrder);
    }

    @Test
    @DisplayName("❌ CANCELLED không thể làm gì cả (terminal state)")
    void testCancelledIsTerminal() {
        OrderContext ctx = ctx(OrderStatus.CANCELLED);
        assertThrows(IllegalStateException.class, ctx::processOrder);
        assertThrows(IllegalStateException.class, ctx::shipOrder);
        assertThrows(IllegalStateException.class, ctx::deliverOrder);
    }
}
