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
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC",
                    Address.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Optional<Address> findById(Long id) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            Address address = em.find(Address.class, id);
            return Optional.ofNullable(address);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Address save(Address address) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            if (address.getId() == null) {
                em.persist(address);
            } else {
                address = em.merge(address);
            }
            DatabaseConfig.getInstance().commitTransaction();
            return address;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Lỗi khi lưu địa chỉ: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void delete(Address address) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            if (!em.contains(address)) {
                address = em.merge(address);
            }
            em.remove(address);
            DatabaseConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Lỗi khi xóa địa chỉ: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void resetDefaultAddress(Long userId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            DatabaseConfig.getInstance().beginTransaction();
            em.createQuery("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
            DatabaseConfig.getInstance().commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Lỗi khi reset địa chỉ mặc định: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
