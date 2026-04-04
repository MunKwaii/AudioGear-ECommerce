package vn.edu.ute.dao;

import vn.edu.ute.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Optional<Order> findById(Long id);
    Optional<Order> findByIdWithItems(Long id);
    List<Order> findAll();
    List<Order> findAllWithItems();
    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderCode(String orderCode);
    Order save(Order order);
}
