package vn.edu.ute.dao;

import vn.edu.ute.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherDao {

    /**
     * Tìm voucher theo mã code.
     */
    Optional<Voucher> findByCode(String code);

    /**
     * Đếm số đơn hàng đã sử dụng voucher này.
     * Dùng để kiểm tra maxUsage.
     */
    long countOrdersUsingVoucher(Long voucherId);

    // Admin CRUD methods
    List<Voucher> findAll();
    Optional<Voucher> findById(Long id);
    Voucher save(Voucher voucher);
    void delete(Long id);
    List<Voucher> search(String keyword, vn.edu.ute.entity.enums.VoucherStatus status, int offset, int limit);
    long countSearch(String keyword, vn.edu.ute.entity.enums.VoucherStatus status);
}