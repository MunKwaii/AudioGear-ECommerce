package vn.edu.ute.service.stats;
 
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
 
/**
 * Factory Pattern: Chuyển đổi mã code khoảng thời gian sang đối tượng TimeRangeStrategy.
 * Tuân thủ Open-Closed Principle (OCP).
 */
public final class TimeRangeStrategyFactory {
 
    private static final Map<String, Supplier<TimeRangeStrategy>> STRATEGIES = Map.of(
            "7d", Last7DaysStrategy::new,
            "30d", Last30DaysStrategy::new,
            "90d", Last90DaysStrategy::new,
            "1y", LastYearStrategy::new,
            "all", AllTimeStrategy::new
    );
 
    private TimeRangeStrategyFactory() {
        // Utility class
    }
 
    /**
     * Lấy chiến lược thống kê từ mã code.
     * Mặc định là 7 ngày nếu mã không hợp lệ.
     */
    public static TimeRangeStrategy fromCode(String code) {
        return Optional.ofNullable(code)
                .map(String::toLowerCase)
                .map(STRATEGIES::get)
                .map(Supplier::get)
                .orElseGet(Last7DaysStrategy::new);
    }
}
