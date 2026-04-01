package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

/**
 * Thanh toán khi nhận hàng (Cash On Delivery).
 * Luôn trả thành công vì chưa có giao dịch thực tế tại thời điểm đặt hàng.
 */
public class CODStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        return PaymentResult.success("Thanh toán khi nhận hàng — COD");
    }

    @Override
    public String getStrategyCode() {
        return "COD";
    }
}