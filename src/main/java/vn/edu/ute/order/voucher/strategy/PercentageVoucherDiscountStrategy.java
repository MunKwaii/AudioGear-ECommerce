package vn.edu.ute.order.voucher.strategy;

import vn.edu.ute.entity.Voucher;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentageVoucherDiscountStrategy implements VoucherDiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        if (voucher == null || orderTotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = orderTotal
                .multiply(voucher.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Không cho giảm vượt quá tổng đơn
        return discount.min(orderTotal);
    }
}