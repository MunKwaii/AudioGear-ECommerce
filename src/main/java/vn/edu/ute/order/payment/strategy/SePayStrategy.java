package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

/**
 * Thanh toán qua mã QR (SePay).
 * Trạng thái của kết quả là pending, yêu cầu quét QR.
 */
public class SePayStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        // Cho biết việc thanh toán đang chờ xử lý qua quét QR
        return new PaymentResult(true, "Vui lòng quét mã QR để hoàn tất thanh toán (SePay).", null);
    }

    @Override
    public String getStrategyCode() {
        return "SEPAY_QR";
    }
}
