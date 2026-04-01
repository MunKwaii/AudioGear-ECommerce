package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.BrandDao;
import vn.edu.ute.entity.Brand;

import java.util.List;
import java.util.Optional;

public class BrandDaoImpl implements BrandDao {

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
}
