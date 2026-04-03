package vn.edu.ute.service;

import vn.edu.ute.dto.response.VoucherValidationResult;
import vn.edu.ute.entity.Voucher;

import java.math.BigDecimal;

import vn.edu.ute.dto.VoucherDTO;
import java.util.List;
import java.util.Optional;

public interface VoucherService {

    /**
     * Kiểm tra voucher có hợp lệ với đơn hàng hiện tại hay không.
     */
    VoucherValidationResult validateVoucher(String voucherCode, BigDecimal orderTotal, Long userId);

    /**
     * Tính số tiền giảm giá từ voucher hợp lệ.
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal);

    /**
     * Áp voucher vào tổng tiền đơn hàng.
     * Trả về số tiền cuối cùng sau khi trừ giảm giá.
     */
    BigDecimal applyVoucher(String voucherCode, BigDecimal orderTotal, Long userId);

    // Admin methods
    List<VoucherDTO> getAllVouchers();
    Optional<VoucherDTO> getVoucherById(Long id);
    Voucher createVoucher(Voucher voucher);
    Voucher updateVoucher(Voucher voucher);
    void deleteVoucher(Long id);
    List<VoucherDTO> searchVouchers(String keyword, vn.edu.ute.entity.enums.VoucherStatus status, int page, int size);
    long countSearch(String keyword, vn.edu.ute.entity.enums.VoucherStatus status);
}