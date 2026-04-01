package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

/**
 * Nhận hàng tại cửa hàng.
 * Luôn trả thành công — khách sẽ thanh toán khi đến lấy hàng.
 */
public class StorePickupStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        return PaymentResult.success("Đặt hàng nhận tại cửa hàng — thanh toán khi nhận");
    }

    @Override
    public String getStrategyCode() {
        return "STORE_PICKUP";
    }
}