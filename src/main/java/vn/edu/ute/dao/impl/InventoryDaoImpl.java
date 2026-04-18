package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.InventoryDao;
import vn.edu.ute.entity.Inventory;
import java.util.Optional;

public class InventoryDaoImpl implements InventoryDao {

    private static InventoryDaoImpl instance;

    private InventoryDaoImpl() {}

    public static synchronized InventoryDaoImpl getInstance() {
        if (instance == null) {
            instance = new InventoryDaoImpl();
        }
        return instance;
    }

    @Override
    public Optional<Inventory> findByProductId(Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            String jpql = "SELECT i FROM Inventory i WHERE i.product.id = :productId";
            TypedQuery<Inventory> query = em.createQuery(jpql, Inventory.class);
            query.setParameter("productId", productId);
            return query.getResultList().stream().findFirst();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Inventory save(Inventory inventory) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            if (inventory.getId() == null) {
                em.persist(inventory);
            } else {
                inventory = em.merge(inventory);
            }
            DatabaseConfig.getInstance().commitTransaction();
            return inventory;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Loi khi luu Inventory: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void delete(Inventory inventory) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            Inventory managed = em.find(Inventory.class, inventory.getId());
            if (managed != null) {
                em.remove(managed);
            }
            DatabaseConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Loi khi xoa Inventory: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
