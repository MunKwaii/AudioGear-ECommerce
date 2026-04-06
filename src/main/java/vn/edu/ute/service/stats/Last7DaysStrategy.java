package vn.edu.ute.service.stats;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
/**
 * Chiến lược thống kê 7 ngày gần nhất.
 */
public class Last7DaysStrategy implements TimeRangeStrategy {
    @Override
    public String getLabel() {
        return "7 ngày gần nhất";
    }
 
    @Override
    public LocalDateTime getFrom() {
        return LocalDate.now().minusDays(6).atStartOfDay();
    }
 
    @Override
    public LocalDateTime getTo() {
        return LocalDateTime.now();
    }
 
    @Override
    public String getCode() {
        return "7d";
    }
}
