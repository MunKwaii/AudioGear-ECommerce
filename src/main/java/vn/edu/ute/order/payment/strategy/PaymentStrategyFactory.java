package vn.edu.ute.order.payment.strategy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Factory Pattern: chuyển đổi mã thanh toán (String) sang đối tượng Strategy.
 * Dùng Supplier để tạo instance mới mỗi lần gọi (tránh chia sẻ state giữa các request).
 *
 * Sử dụng lambda và Map.ofEntries thay cho switch-case truyền thống.
 */
public final class PaymentStrategyFactory {

    private static final Map<String, Supplier<PaymentStrategy>> STRATEGIES = Map.of(
            "COD", CODStrategy::new,
            "MOMO", MomoStrategy::new,
            "BANK", BankTransferStrategy::new,
            "BANK_TRANSFER", BankTransferStrategy::new,
            "STORE_PICKUP", StorePickupStrategy::new
    );

    private PaymentStrategyFactory() {
        // Utility class — không cho phép tạo instance
    }

    /**
     * Chuyển mã thanh toán từ request sang Strategy object.
     * Nếu mã không hợp lệ → ném RuntimeException.
     */
    public static PaymentStrategy fromCode(String code) {
        return Optional.ofNullable(code)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(STRATEGIES::get)
                .map(Supplier::get)
                .orElseThrow(() -> new RuntimeException(
                        "Phương thức thanh toán không hợp lệ: " + code));
    }

    /**
     * Kiểm tra mã thanh toán có hợp lệ không (không ném exception).
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.isBlank()) return false;
        return STRATEGIES.containsKey(code.trim().toUpperCase());
    }
}
