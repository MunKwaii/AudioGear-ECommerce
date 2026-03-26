package vn.edu.ute.dao;

import vn.edu.ute.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Optional<Order> findById(Long id);
    List<Order> findAll();
    Order save(Order order);
}
