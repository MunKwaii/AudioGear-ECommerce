package vn.edu.ute.dao;

import vn.edu.ute.entity.Voucher;

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
}