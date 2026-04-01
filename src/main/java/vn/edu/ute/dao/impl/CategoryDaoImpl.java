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
}
