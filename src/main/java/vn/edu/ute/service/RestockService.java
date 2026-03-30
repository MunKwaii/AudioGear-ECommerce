package vn.edu.ute.service;

import vn.edu.ute.entity.Order;

/**
 * Interface cho việc hoàn trả số lượng sản phẩm vào kho
 * khi Đơn hàng bị huỷ (dù do Admin từ chối hay giao hàng thất bại).
 *
 * Áp dụng Single Responsibility: tách biệt logic restock
 * ra khỏi State layer để dễ test và thay thế.
 */
public interface RestockService {

    /**
     * Cộng lại số lượng sản phẩm vào kho cho tất cả items trong order bị huỷ.
     * Order phải đã được load kèm items (JOIN FETCH) trước khi gọi method này.
     *
     * @param order Đơn hàng bị huỷ (đã load eager items)
     */
    void restoreStock(Order order);
}
