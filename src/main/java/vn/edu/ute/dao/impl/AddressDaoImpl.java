package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.AddressDao;
import vn.edu.ute.entity.Address;

import java.util.List;
import java.util.Optional;

public class AddressDaoImpl implements AddressDao {

    @Override
    public List<Address> findByUserId(Long userId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC", Address.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<Address> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Address address = em.find(Address.class, id);
            return Optional.ofNullable(address);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Address save(Address address) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (address.getId() == null) {
                em.persist(address);
            } else {
                address = em.merge(address);
            }
            DatabaseConfig.commitTransaction();
            return address;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi lưu địa chỉ: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public void delete(Address address) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (!em.contains(address)) {
                address = em.merge(address);
            }
            em.remove(address);
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi xóa địa chỉ: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public void resetDefaultAddress(Long userId) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            em.createQuery("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
              .setParameter("userId", userId)
              .executeUpdate();
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi reset địa chỉ mặc định: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
