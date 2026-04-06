package vn.edu.ute.service.stats;
 
import java.time.LocalDateTime;
 
/**
 * Strategy Interface: Định nghĩa các khoảng thời gian thống kê.
 * Tuân thủ Open-Closed Principle (OCP).
 */
public interface TimeRangeStrategy {
    String getLabel();
    LocalDateTime getFrom();
    LocalDateTime getTo();
    String getCode();
}
