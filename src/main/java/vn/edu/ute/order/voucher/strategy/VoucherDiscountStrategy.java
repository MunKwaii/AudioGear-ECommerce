package vn.edu.ute.order.voucher.strategy;

import vn.edu.ute.entity.Voucher;

import java.math.BigDecimal;

public interface VoucherDiscountStrategy {

    /**
     * Tính số tiền giảm cho đơn hàng.
     *
     * @param voucher    voucher hợp lệ
     * @param orderTotal tổng tiền gốc của đơn hàng
     * @return số tiền được giảm
     */
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal);
}