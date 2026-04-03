package vn.edu.ute.controller.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.StatsReportDTO;
import vn.edu.ute.service.stats.StatsReportService;
import vn.edu.ute.service.stats.impl.StatsReportServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/admin/stats-report")
public class AdminStatsReportController extends HttpServlet {

    private final StatsReportService statsReportService = new StatsReportServiceImpl();
    private final Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy HH:mm").create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String timeRange = req.getParameter("range");
        if (timeRange == null || timeRange.trim().isEmpty()) {
            timeRange = "7d";
        }

        try {
            StatsReportDTO report = statsReportService.generateReport(timeRange);
            Map<String, Object> result = toResponseMap(report);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("success", false, "message", e.getMessage())));
        }
    }

    private Map<String, Object> toResponseMap(StatsReportDTO report) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);

        Map<String, Object> period = new LinkedHashMap<>();
        period.put("label", report.getPeriod().getLabel());
        period.put("from", report.getPeriod().getFrom());
        period.put("to", report.getPeriod().getTo());
        result.put("period", period);

        Map<String, Object> revenue = new LinkedHashMap<>();
        revenue.put("total", report.getRevenue().getTotal());
        revenue.put("avgOrderValue", report.getRevenue().getAvgOrderValue());
        revenue.put("highestOrder", report.getRevenue().getHighestOrder());
        revenue.put("lowestOrder", report.getRevenue().getLowestOrder());
        result.put("revenue", revenue);

        Map<String, Object> orders = new LinkedHashMap<>();
        orders.put("total", report.getOrders().getTotal());
        orders.put("completed", report.getOrders().getCompleted());
        orders.put("cancelled", report.getOrders().getCancelled());
        orders.put("pending", report.getOrders().getPending());
        orders.put("processing", report.getOrders().getProcessing());
        orders.put("shipped", report.getOrders().getShipped());
        orders.put("completionRate", report.getOrders().getCompletionRate());
        result.put("orders", orders);

        Map<String, Object> products = new LinkedHashMap<>();
        products.put("total", report.getProducts().getTotal());
        products.put("active", report.getProducts().getActive());
        products.put("inactive", report.getProducts().getInactive());
        products.put("lowStock", report.getProducts().getLowStock());
        products.put("outOfStock", report.getProducts().getOutOfStock());
        products.put("avgPrice", report.getProducts().getAvgPrice());
        result.put("products", products);

        Map<String, Object> users = new LinkedHashMap<>();
        users.put("total", report.getUsers().getTotal());
        users.put("active", report.getUsers().getActive());
        users.put("locked", report.getUsers().getLocked());
        users.put("pending", report.getUsers().getPending());
        users.put("adminCount", report.getUsers().getAdminCount());
        users.put("customerCount", report.getUsers().getCustomerCount());
        result.put("users", users);

        Map<String, Object> brands = new LinkedHashMap<>();
        brands.put("total", report.getBrands().getTotal());
        brands.put("withProducts", report.getBrands().getWithProducts());
        brands.put("withoutProducts", report.getBrands().getWithoutProducts());
        result.put("brands", brands);

        Map<String, Object> categories = new LinkedHashMap<>();
        categories.put("total", report.getCategories().getTotal());
        categories.put("withProducts", report.getCategories().getWithProducts());
        categories.put("withoutProducts", report.getCategories().getWithoutProducts());
        result.put("categories", categories);

        result.put("revenueByDay", report.getRevenueByDay().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue())));

        result.put("ordersByStatus", report.getOrdersByStatus());
        result.put("salesByBrand", report.getSalesByBrand());
        result.put("salesByCategory", report.getSalesByCategory());

        result.put("topSellingProducts", report.getTopSellingProducts().stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", p.getName());
                    m.put("thumbnailUrl", p.getThumbnailUrl());
                    m.put("quantitySold", p.getQuantitySold());
                    m.put("totalRevenue", p.getTotalRevenue());
                    return m;
                }).collect(Collectors.toList()));

        result.put("topRevenueProducts", report.getTopRevenueProducts().stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", p.getName());
                    m.put("thumbnailUrl", p.getThumbnailUrl());
                    m.put("quantitySold", p.getQuantitySold());
                    m.put("totalRevenue", p.getTotalRevenue());
                    return m;
                }).collect(Collectors.toList()));

        return result;
    }
}
