package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.entity.Product;

import java.util.Optional;

public class ProductDaoImpl implements ProductDao {

    @Override
    public Product save(Product product) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (product.getId() == null) {
                em.persist(product);
            } else {
                product = em.merge(product);
            }
            DatabaseConfig.commitTransaction();
            return product;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Loi khi luu Product: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Product product = em.find(Product.class, id);
            return Optional.ofNullable(product);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
