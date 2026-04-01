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

    // Áp dụng Singleton Pattern (Lazy Initialization)
    private static ProductDaoImpl instance;

    private ProductDaoImpl() {
        // Private constructor để ngăn việc khởi tạo từ bên ngoài
    }

    public static synchronized ProductDaoImpl getInstance() {
        if (instance == null) {
            instance = new ProductDaoImpl();
        }
        return instance;
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Lấy các sản phẩm đang active (status = true), sắp xếp theo giá giảm dần làm ví dụ
            String jpql = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand WHERE p.status = true ORDER BY p.price DESC";
            TypedQuery<Product> query = em.createQuery(jpql, Product.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Product> getNewestProducts(int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Lấy các sản phẩm đang active (status = true), mới nhất theo ngày tạo
            String jpql = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand WHERE p.status = true ORDER BY p.createdAt DESC";
            TypedQuery<Product> query = em.createQuery(jpql, Product.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Product> searchProducts(String keyword, Long categoryId, int offset, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM Product p JOIN FETCH p.category c LEFT JOIN FETCH p.brand WHERE p.status = true");
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND c.id = :categoryId");
            }
            jpql.append(" ORDER BY p.createdAt DESC"); // Sắp xếp mới nhất trên cùng

            TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public long countSearchProducts(String keyword, Long categoryId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Dùng COUNT(p) để đếm và tối ưu join, KHÔNG dùng FETCH JOIN vì nó ko hợp lệ lệnh Count Query của Hibernate.
            StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p JOIN p.category c WHERE p.status = true");
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND c.id = :categoryId");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            
            return query.getSingleResult();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
