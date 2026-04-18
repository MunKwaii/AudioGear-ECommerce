package vn.edu.ute.dao;

import vn.edu.ute.entity.Inventory;
import java.util.Optional;

public interface InventoryDao {
    Optional<Inventory> findByProductId(Long productId);
    Inventory save(Inventory inventory);
    void delete(Inventory inventory);
}
