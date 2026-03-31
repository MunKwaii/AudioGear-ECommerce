package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.entity.Category;

import java.util.List;

public class CategoryDaoImpl implements CategoryDao {

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
