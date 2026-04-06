package vn.edu.ute.service.stats;
 
import java.time.LocalDateTime;
 
/**
 * Chiến lược thống kê toàn bộ thời gian.
 */
public class AllTimeStrategy implements TimeRangeStrategy {
    @Override
    public String getLabel() {
        return "Toàn bộ thời gian";
    }
 
    @Override
    public LocalDateTime getFrom() {
        return LocalDateTime.of(2024, 1, 1, 0, 0);
    }
 
    @Override
    public LocalDateTime getTo() {
        return LocalDateTime.now();
    }
 
    @Override
    public String getCode() {
        return "all";
    }
}
