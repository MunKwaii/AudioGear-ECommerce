package vn.edu.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.OrderItem;
import vn.edu.ute.entity.Product;
import vn.edu.ute.service.RestockService;

/**
 * Triển khai RestockService: hoàn trả số lượng sản phẩm vào kho
 * khi một Đơn hàng bị huỷ (dù ở giai đoạn PENDING hay SHIPPED).
 *
 * Lưu ý quan trọng:
 * - Order phải được load kèm items và product (JOIN FETCH) trước khi gọi.
 * - Mỗi item sẽ cộng lại quantity vào Product.stockQuantity và lưu DB.
 */
public class RestockServiceImpl implements RestockService {

    @Override
    public void restoreStock(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.out.printf("[RESTOCK] Đơn hàng #%s không có items để hoàn kho.%n", order.getOrderCode());
            return;
        }

        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product == null) continue;

                // Merge để lấy managed entity trong transaction hiện tại
                Product managedProduct = em.merge(product);
                int restored = item.getQuantity() != null ? item.getQuantity() : 0;
                int newStock = 0;
                if (managedProduct.getInventory() != null) {
                    managedProduct.getInventory().setStockQuantity(managedProduct.getInventory().getStockQuantity() + restored);
                    newStock = managedProduct.getInventory().getStockQuantity();
                }

                System.out.printf("[RESTOCK] Sản phẩm \"%s\" (ID=%d): +%d đơn vị → Tồn kho mới: %d%n",
                        managedProduct.getName(), managedProduct.getId(),
                        restored, newStock);
            }

            DatabaseConfig.getInstance().commitTransaction();
            System.out.printf("[RESTOCK] Hoàn tất hoàn kho cho Đơn hàng #%s.%n", order.getOrderCode());

        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Lỗi khi hoàn trả tồn kho cho đơn hàng #" + order.getOrderCode() + ": " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
