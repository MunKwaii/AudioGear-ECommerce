package vn.edu.ute.dao;

import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithItems(Long id);
     Optional<Order> findByIdWithUserAndItems(Long id);
    List<Order> findAll();
    List<Order> findAllWithItems();
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByOrderCodeWithItems(String orderCode);
    List<Order> findByOrderCodes(List<String> orderCodes);
    Order save(Order order);
    void updateStatus(String orderCode, OrderStatus status);
}
