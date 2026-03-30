package vn.edu.ute.dao;

import vn.edu.ute.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    Optional<Order> findById(Long id);

    /**
     * Load Order kèm OrderItems và Product bằng JOIN FETCH.
     * Dùng khi cần truy cập items (ví dụ: RestockService).
     */
    Optional<Order> findByIdWithItems(Long id);

    List<Order> findAll();
    Order save(Order order);
}
