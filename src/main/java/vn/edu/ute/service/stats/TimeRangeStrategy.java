package vn.edu.ute.service.stats;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Strategy Pattern: Định nghĩa các khoảng thời gian thống kê.
 */
public interface TimeRangeStrategy {
    String getLabel();
    LocalDateTime getFrom();
    LocalDateTime getTo();
    String getCode();

    static TimeRangeStrategy fromCode(String code) {
        return switch (code) {
            case "7d" -> new Last7DaysStrategy();
            case "30d" -> new Last30DaysStrategy();
            case "90d" -> new Last90DaysStrategy();
            case "1y" -> new LastYearStrategy();
            case "all" -> new AllTimeStrategy();
            default -> new Last7DaysStrategy();
        };
    }
}

class Last7DaysStrategy implements TimeRangeStrategy {
    public String getLabel() { return "7 ngày gần nhất"; }
    public LocalDateTime getFrom() { return LocalDate.now().minusDays(6).atStartOfDay(); }
    public LocalDateTime getTo() { return LocalDateTime.now(); }
    public String getCode() { return "7d"; }
}

class Last30DaysStrategy implements TimeRangeStrategy {
    public String getLabel() { return "30 ngày gần nhất"; }
    public LocalDateTime getFrom() { return LocalDate.now().minusDays(29).atStartOfDay(); }
    public LocalDateTime getTo() { return LocalDateTime.now(); }
    public String getCode() { return "30d"; }
}

class Last90DaysStrategy implements TimeRangeStrategy {
    public String getLabel() { return "90 ngày gần nhất"; }
    public LocalDateTime getFrom() { return LocalDate.now().minusDays(89).atStartOfDay(); }
    public LocalDateTime getTo() { return LocalDateTime.now(); }
    public String getCode() { return "90d"; }
}

class LastYearStrategy implements TimeRangeStrategy {
    public String getLabel() { return "1 năm gần nhất"; }
    public LocalDateTime getFrom() { return LocalDate.now().minusYears(1).atStartOfDay(); }
    public LocalDateTime getTo() { return LocalDateTime.now(); }
    public String getCode() { return "1y"; }
}

class AllTimeStrategy implements TimeRangeStrategy {
    public String getLabel() { return "Toàn bộ thời gian"; }
    public LocalDateTime getFrom() { return LocalDateTime.of(2024, 1, 1, 0, 0); }
    public LocalDateTime getTo() { return LocalDateTime.now(); }
    public String getCode() { return "all"; }
}
