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
        } finally {
            DatabaseConfig.closeEntityManager();
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
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}