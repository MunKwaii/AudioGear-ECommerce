package vn.edu.ute.order.voucher.strategy;

import vn.edu.ute.entity.enums.DiscountType;

public class VoucherDiscountStrategyFactory {

    public VoucherDiscountStrategy getStrategy(DiscountType discountType) {
        if (discountType == null) {
            throw new IllegalArgumentException("Discount type không được null");
        }

        return switch (discountType) {
            case PERCENTAGE -> new PercentageVoucherDiscountStrategy();
            case FIXED_AMOUNT -> new FixedAmountVoucherDiscountStrategy();
        };
    }
}