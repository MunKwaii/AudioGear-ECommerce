package vn.edu.ute.order.voucher.strategy;

import vn.edu.ute.entity.Voucher;

import java.math.BigDecimal;

public class FixedAmountVoucherDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        if (voucher == null || orderTotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = voucher.getDiscountValue();
        return discount.min(orderTotal);
    }
}