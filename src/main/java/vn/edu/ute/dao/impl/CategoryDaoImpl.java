package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.entity.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CategoryDaoImpl implements CategoryDao {

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
    public Optional<Category> findById(Long id) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.id = :id", Category.class);
            query.setParameter("id", id);
            List<Category> results = query.getResultList();
            return results.stream().findFirst();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Optional<Category> findByIdWithChildren(Long id) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id", Category.class);
            query.setParameter("id", id);
            List<Category> results = query.getResultList();
            return results.stream().findFirst();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public List<Category> findAll() {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c LEFT JOIN FETCH c.parent ORDER BY c.name ASC", Category.class)
                    .getResultList();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public List<Category> getAllCategories() {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            String jpql = "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name ASC";
            TypedQuery<Category> query = em.createQuery(jpql, Category.class);
            return query.getResultList();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public List<Category> findRootCategories() {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            TypedQuery<Category> query = em.createQuery(
                    "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name ASC", Category.class);
            return query.getResultList();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Category save(Category category) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            em.persist(category);
            DatabaseConfig.getInstance().commitTransaction();
            return category;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Category update(Category category) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            Category merged = em.merge(category);
            DatabaseConfig.getInstance().commitTransaction();
            return merged;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void detachChildren(Long parentId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            em.createQuery("UPDATE Category c SET c.parent = null WHERE c.parent.id = :parentId")
                    .setParameter("parentId", parentId)
                    .executeUpdate();
            DatabaseConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            Category category = em.find(Category.class, id);
            if (category != null) {
                em.remove(category);
            }
            DatabaseConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public long countProductsByCategory(Long categoryId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId", Long.class);
            query.setParameter("categoryId", categoryId);
            return query.getSingleResult();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Map<Long, Long> getProductCountsGroupedByCategory() {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            String jpql = "SELECT p.category.id, COUNT(p) FROM Product p GROUP BY p.category.id";
            List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();
            
            Map<Long, Long> counts = new HashMap<>();
            if (results != null) {
                for (Object[] result : results) {
                    if (result[0] != null) {
                        counts.put((Long) result[0], (Long) result[1]);
                    }
                }
            }
            return counts;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
