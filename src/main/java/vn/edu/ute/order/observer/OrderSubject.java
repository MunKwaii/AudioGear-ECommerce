package vn.edu.ute.order.observer;

import vn.edu.ute.entity.Order;

/**
 * Subject Interface (Observer Pattern).
 *
 * Bất kỳ class nào muốn đóng vai "Subject" (phát sinh sự kiện)
 * đều phải implement interface này.
 * Hiện tại: OrderServiceImpl.
 */
public interface OrderSubject {
    void addObserver(OrderObserver observer);
    void removeObserver(OrderObserver observer);
    void notifyObservers(Order order, OrderEventType event, String reason);
}
