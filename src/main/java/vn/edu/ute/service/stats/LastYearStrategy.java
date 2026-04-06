package vn.edu.ute.service.stats;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
/**
 * Chiến lược thống kê 1 năm gần nhất.
 */
public class LastYearStrategy implements TimeRangeStrategy {
    @Override
    public String getLabel() {
        return "1 năm gần nhất";
    }
 
    @Override
    public LocalDateTime getFrom() {
        return LocalDate.now().minusYears(1).atStartOfDay();
    }
 
    @Override
    public LocalDateTime getTo() {
        return LocalDateTime.now();
    }
 
    @Override
    public String getCode() {
        return "1y";
    }
}
