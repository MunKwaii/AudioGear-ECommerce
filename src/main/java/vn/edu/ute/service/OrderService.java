package vn.edu.ute.service;

import vn.edu.ute.entity.Order;
import java.util.List;

public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    Order processOrder(Long orderId);
    Order shipOrder(Long orderId);
    Order deliverOrder(Long orderId);
    Order cancelOrder(Long orderId, String reason);
    Order cancelOrderByUser(Long userId, Long orderId);
    List<Order> getOrdersByUserId(Long userId);
    Order getOrderByOrderCode(String orderCode);
    List<Order> getOrdersByOrderCodes(List<String> orderCodes);
}
