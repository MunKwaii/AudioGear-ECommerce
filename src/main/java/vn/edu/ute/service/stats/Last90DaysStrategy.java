package vn.edu.ute.service.stats;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
/**
 * Chiến lược thống kê 90 ngày gần nhất.
 */
public class Last90DaysStrategy implements TimeRangeStrategy {
    @Override
    public String getLabel() {
        return "90 ngày gần nhất";
    }
 
    @Override
    public LocalDateTime getFrom() {
        return LocalDate.now().minusDays(89).atStartOfDay();
    }
 
    @Override
    public LocalDateTime getTo() {
        return LocalDateTime.now();
    }
 
    @Override
    public String getCode() {
        return "90d";
    }
}
