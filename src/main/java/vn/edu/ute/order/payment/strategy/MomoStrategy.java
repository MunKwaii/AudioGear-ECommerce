package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Thanh toán qua Ví Momo.
 * Hiện tại: mock tạo transactionId (UUID).
 * Tương lai: tích hợp Momo API thật.
 */
public class MomoStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        String transactionId = "MOMO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return PaymentResult.success("Thanh toán qua Momo thành công", transactionId);
    }

    @Override
    public String getStrategyCode() {
        return "MOMO";
    }
}
