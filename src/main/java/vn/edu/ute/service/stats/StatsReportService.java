package vn.edu.ute.service.stats;

import vn.edu.ute.dto.StatsReportDTO;

public interface StatsReportService {
    StatsReportDTO generateReport(String timeRangeCode);
}
