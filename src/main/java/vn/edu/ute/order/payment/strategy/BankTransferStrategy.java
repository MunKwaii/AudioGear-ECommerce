package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Thanh toán chuyển khoản ngân hàng.
 * Hiện tại: mock tạo transactionId (UUID).
 * Tương lai: tích hợp VNPay / ngân hàng thật.
 */
public class BankTransferStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        String transactionId = "BANK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        return PaymentResult.success("Yêu cầu chuyển khoản đã được ghi nhận", transactionId);
    }

    @Override
    public String getStrategyCode() {
        return "BANK";
    }
}