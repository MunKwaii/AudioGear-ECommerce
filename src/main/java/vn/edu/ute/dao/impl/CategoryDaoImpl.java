package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.entity.Category;

import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {

    @Override
    public Optional<Category> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Category category = em.find(Category.class, id);
            return Optional.ofNullable(category);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Category> findAll() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.name ASC", Category.class)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    // Áp dụng Singleton Pattern (Lazy Initialization)
    private static CategoryDaoImpl instance;

    private CategoryDaoImpl() {
    }

    public static synchronized CategoryDaoImpl getInstance() {
        if (instance == null) {
            instance = new CategoryDaoImpl();
        }
        return instance;
    }

    @Override
    public List<Category> getAllCategories() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            // Lấy tất cả category cha (không có parent) hoặc tất cả
            String jpql = "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
