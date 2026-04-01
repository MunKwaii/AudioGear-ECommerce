package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern: mỗi phương thức thanh toán (COD, Momo, BankTransfer, StorePickup)
 * implement interface này để xử lý logic thanh toán riêng.
 *
 * PaymentStrategyFactory sẽ chịu trách nhiệm tạo đúng Strategy từ mã.
 */
public interface PaymentStrategy {

    /**
     * Thực hiện xử lý thanh toán.
     *
     * @param amount Số tiền cần thanh toán
     * @return PaymentResult chứa kết quả xử lý (thành công/thất bại + transactionId)
     */
    PaymentResult pay(BigDecimal amount);

    /**
     * Trả về mã strategy (dùng cho JPA Converter lưu vào DB).
     * Ví dụ: "COD", "MOMO", "BANK", "STORE_PICKUP"
     */
    String getStrategyCode();
}