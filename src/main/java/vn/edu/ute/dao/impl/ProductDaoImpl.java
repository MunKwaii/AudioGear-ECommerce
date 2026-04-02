package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.entity.Product;

import java.util.List;
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
    public void deleteById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            Product product = em.find(Product.class, id);
            if (product != null) {
                em.remove(product);
            }
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Loi khi xoa Product: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String jpql = "SELECT DISTINCT p FROM Product p " +
                    "LEFT JOIN FETCH p.category " +
                    "LEFT JOIN FETCH p.brand " +
                    "LEFT JOIN FETCH p.images " +
                    "WHERE p.id = :id";
            TypedQuery<Product> query = em.createQuery(jpql, Product.class);
            query.setParameter("id", id);
            return query.getResultList().stream().findFirst();
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
    public List<Product> searchProducts(String keyword, Long categoryId, String sort, int offset, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Product p JOIN FETCH p.category c LEFT JOIN FETCH p.brand WHERE p.status = true");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND (c.id = :categoryId OR c.parent.id = :categoryId)");
            }

            // Xử lý sắp xếp
            if ("price_asc".equals(sort)) {
                jpql.append(" ORDER BY p.price ASC");
            } else if ("price_desc".equals(sort)) {
                jpql.append(" ORDER BY p.price DESC");
            } else {
                jpql.append(" ORDER BY p.createdAt DESC"); // Mặc định: Mới nhất
            }

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
            // Dùng COUNT(p) để đếm và tối ưu join
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) FROM Product p JOIN p.category c WHERE p.status = true");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND (c.id = :categoryId OR c.parent.id = :categoryId)");
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

    @Override
    public List<Product> searchProductsForAdmin(String keyword, Long categoryId, Boolean status, int offset, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT p FROM Product p JOIN FETCH p.category c LEFT JOIN FETCH p.brand b WHERE 1=1");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND c.id = :categoryId");
            }
            if (status != null) {
                jpql.append(" AND p.status = :status");
            }
            jpql.append(" ORDER BY p.createdAt DESC");

            TypedQuery<Product> query = em.createQuery(jpql.toString(), Product.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            if (status != null) {
                query.setParameter("status", status);
            }

            query.setFirstResult(offset);
            query.setMaxResults(limit);

            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public long countSearchProductsForAdmin(String keyword, Long categoryId, Boolean status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT COUNT(p) FROM Product p JOIN p.category c WHERE 1=1");

            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(p.name) LIKE LOWER(:keyword)");
            }
            if (categoryId != null) {
                jpql.append(" AND c.id = :categoryId");
            }
            if (status != null) {
                jpql.append(" AND p.status = :status");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("keyword", "%" + keyword.trim() + "%");
            }
            if (categoryId != null) {
                query.setParameter("categoryId", categoryId);
            }
            if (status != null) {
                query.setParameter("status", status);
            }

            return query.getSingleResult();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Product> findRelatedProducts(Long categoryId, Long excludeProductId, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            String jpql = "SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.brand " +
                    "WHERE p.category.id = :categoryId AND p.id != :excludeProductId " +
                    "AND p.status = true ORDER BY p.createdAt DESC";
            TypedQuery<Product> query = em.createQuery(jpql, Product.class);
            query.setParameter("categoryId", categoryId);
            query.setParameter("excludeProductId", excludeProductId);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
