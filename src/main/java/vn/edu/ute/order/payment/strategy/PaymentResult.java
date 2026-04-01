package vn.edu.ute.order.payment.strategy;

/**
 * Kết quả xử lý thanh toán trả về từ mỗi PaymentStrategy.
 * Record giúp immutable và gọn gàng cho DTO đơn giản.
 *
 * @param success       Thanh toán thành công hay không
 * @param message       Thông báo mô tả kết quả
 * @param transactionId Mã giao dịch (null nếu thanh toán tại chỗ như COD/StorePickup)
 */
public record PaymentResult(boolean success, String message, String transactionId) {

    /**
     * Factory method: tạo PaymentResult thành công (không có transactionId).
     */
    public static PaymentResult success(String message) {
        return new PaymentResult(true, message, null);
    }

    /**
     * Factory method: tạo PaymentResult thành công (có transactionId).
     */
    public static PaymentResult success(String message, String transactionId) {
        return new PaymentResult(true, message, transactionId);
    }

    /**
     * Factory method: tạo PaymentResult thất bại.
     */
    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message, null);
    }
}
