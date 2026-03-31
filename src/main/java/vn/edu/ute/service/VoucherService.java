package vn.edu.ute.service;

import vn.edu.ute.dto.response.VoucherValidationResult;
import vn.edu.ute.entity.Voucher;

import java.math.BigDecimal;

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
}