package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.VoucherDao;
import vn.edu.ute.entity.Voucher;

import java.util.List;
import java.util.Optional;

public class VoucherDaoImpl implements VoucherDao {

    @Override
    public Optional<Voucher> findByCode(String code) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            List<Voucher> results = em.createQuery(
                    "SELECT v FROM Voucher v WHERE UPPER(v.code) = UPPER(:code)",
                    Voucher.class).setParameter("code", code)
                    .getResultList();

            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm Voucher theo code: " + e.getMessage(), e);
        }
    }

    @Override
    public long countOrdersUsingVoucher(Long voucherId) {
        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            Long count = em.createQuery(
                    "SELECT COUNT(o) FROM Order o WHERE o.voucher.id = :voucherId",
                    Long.class).setParameter("voucherId", voucherId)
                    .getSingleResult();

            return count == null ? 0 : count;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đếm đơn hàng sử dụng Voucher: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Voucher> findAll() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Voucher v ORDER BY v.createdAt DESC", Voucher.class)
                    .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy danh sách Voucher: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Voucher> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            Voucher voucher = em.find(Voucher.class, id);
            return Optional.ofNullable(voucher);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm Voucher theo ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Voucher save(Voucher voucher) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (voucher.getId() == null) {
                em.persist(voucher);
            } else {
                voucher = em.merge(voucher);
            }
            DatabaseConfig.commitTransaction();
            return voucher;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi lưu Voucher: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            Voucher voucher = em.find(Voucher.class, id);
            if (voucher != null) {
                em.remove(voucher);
            }
            DatabaseConfig.commitTransaction();
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi xóa Voucher: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Voucher> search(String keyword, vn.edu.ute.entity.enums.VoucherStatus status, int offset, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT v FROM Voucher v WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(v.code) LIKE :kw");
            }
            if (status != null) {
                jpql.append(" AND v.status = :status");
            }
            jpql.append(" ORDER BY v.createdAt DESC");

            jakarta.persistence.TypedQuery<Voucher> query = em.createQuery(jpql.toString(), Voucher.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (status != null) {
                query.setParameter("status", status);
            }
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tìm kiếm Voucher: " + e.getMessage(), e);
        }
    }

    @Override
    public long countSearch(String keyword, vn.edu.ute.entity.enums.VoucherStatus status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(v) FROM Voucher v WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND LOWER(v.code) LIKE :kw");
            }
            if (status != null) {
                jpql.append(" AND v.status = :status");
            }

            jakarta.persistence.TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (status != null) {
                query.setParameter("status", status);
            }
            return query.getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đếm kết quả tìm kiếm Voucher: " + e.getMessage(), e);
        }
    }
}