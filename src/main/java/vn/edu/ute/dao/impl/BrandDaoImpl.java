package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.BrandDao;
import vn.edu.ute.entity.Brand;

import java.util.List;
import java.util.Optional;

public class BrandDaoImpl implements BrandDao {

    private static BrandDaoImpl instance;

    private BrandDaoImpl() {
    }

    public static synchronized BrandDaoImpl getInstance() {
        if (instance == null) {
            instance = new BrandDaoImpl();
        }
        return instance;
    }

    @Override
    public Optional<Brand> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Brand brand = em.find(Brand.class, id);
            return Optional.ofNullable(brand);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public List<Brand> findAll() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT b FROM Brand b ORDER BY b.name ASC", Brand.class)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Brand save(Brand brand) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            em.persist(brand);
            DatabaseConfig.commitTransaction();
            return brand;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Brand update(Brand brand) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            Brand merged = em.merge(brand);
            DatabaseConfig.commitTransaction();
            return merged;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            Brand brand = em.find(Brand.class, id);
            if (brand != null) {
                em.remove(brand);
            }
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Brand> findByName(String name) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Brand> query = em.createQuery(
                    "SELECT b FROM Brand b WHERE b.name = :name", Brand.class);
            query.setParameter("name", name);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public long countProductsByBrandId(Long brandId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE p.brand.id = :brandId", Long.class);
            query.setParameter("brandId", brandId);
            return query.getSingleResult();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
