package vn.edu.ute.service.stats;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
/**
 * Chiến lược thống kê 30 ngày gần nhất.
 */
public class Last30DaysStrategy implements TimeRangeStrategy {
    @Override
    public String getLabel() {
        return "30 ngày gần nhất";
    }
 
    @Override
    public LocalDateTime getFrom() {
        return LocalDate.now().minusDays(29).atStartOfDay();
    }
 
    @Override
    public LocalDateTime getTo() {
        return LocalDateTime.now();
    }
 
    @Override
    public String getCode() {
        return "30d";
    }
}
