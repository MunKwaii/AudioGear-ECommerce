package vn.edu.ute.service.impl;

import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.dao.impl.OrderDaoImpl;
import vn.edu.ute.entity.Order;
import vn.edu.ute.order.observer.EmailOrderObserver;
import vn.edu.ute.order.observer.OrderEventType;
import vn.edu.ute.order.observer.OrderObserver;
import vn.edu.ute.order.observer.OrderSubject;
import vn.edu.ute.order.state.OrderContext;
import vn.edu.ute.service.OrderService;

import java.util.ArrayList;
import java.util.List;

/**
 * OrderServiceImpl đóng 2 vai:
 * 1. Service: thực hiện logic nghiệp vụ đơn hàng (State Machine)
 * 2. Subject (Observer Pattern): thông báo cho các Observer sau mỗi thay đổi trạng thái
 */
public class OrderServiceImpl implements OrderService, OrderSubject {

    private final OrderDao orderDao;

    // Danh sách Observer đang "đăng ký lắng nghe"
    private final List<OrderObserver> observers = new ArrayList<>();

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
        // Đăng ký EmailOrderObserver mặc định
        this.addObserver(new EmailOrderObserver());
    }

    // ── OrderSubject: Quản lý Observer ───────────────────────────────
    @Override
    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Order order, OrderEventType event, String reason) {
        for (OrderObserver observer : observers) {
            try {
                observer.onOrderEvent(order, event, reason);
            } catch (Exception e) {
                // Observer không được làm crash luồng chính
                System.err.println("[Observer Error] " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    // ── OrderService ─────────────────────────────────────────────────
    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + id));
    }

    @Override
    public Order processOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        context.processOrder();
        Order saved = orderDao.save(context.getOrder());
        // Notify: Admin đã duyệt đơn → gửi email xác nhận đang xử lý
        notifyObservers(saved, OrderEventType.ORDER_PROCESSING, null);
        return saved;
    }

    @Override
    public Order shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        context.shipOrder();
        Order saved = orderDao.save(context.getOrder());
        // Notify: Hàng đã xuất kho → gửi email đang giao
        notifyObservers(saved, OrderEventType.ORDER_SHIPPED, null);
        return saved;
    }

    @Override
    public Order deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        context.deliverOrder();
        Order saved = orderDao.save(context.getOrder());
        // Notify: Giao thành công → gửi email cảm ơn
        notifyObservers(saved, OrderEventType.ORDER_DELIVERED, null);
        return saved;
    }

    @Override
    public Order cancelOrder(Long orderId, String reason) {
        // JOIN FETCH items+product để restoreStock có thể duyệt được items
        Order order = orderDao.findByIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));
        OrderContext context = new OrderContext(order);
        String displayReason = (reason != null && !reason.isBlank()) ? reason : "Không có lý do cụ thể";
        context.cancelOrder(displayReason);
        Order saved = orderDao.save(context.getOrder());
        // Notify: Đơn bị huỷ → gửi email thông báo có lý do
        notifyObservers(saved, OrderEventType.ORDER_CANCELLED, displayReason);
        return saved;
    }
}
