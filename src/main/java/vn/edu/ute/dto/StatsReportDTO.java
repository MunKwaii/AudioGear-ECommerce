package vn.edu.ute.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa toàn bộ chỉ số thống kê chi tiết.
 * Sử dụng Builder Pattern.
 */
public class StatsReportDTO {

    private final PeriodInfo period;
    private final RevenueStats revenue;
    private final OrderStats orders;
    private final ProductStats products;
    private final UserStats users;
    private final BrandStats brands;
    private final CategoryStats categories;
    private final Map<String, BigDecimal> revenueByDay;
    private final Map<String, Integer> ordersByStatus;
    private final Map<String, Long> salesByBrand;
    private final Map<String, Long> salesByCategory;
    private final List<TopProductDTO> topSellingProducts;
    private final List<TopProductDTO> topRevenueProducts;

    private StatsReportDTO(Builder builder) {
        this.period = builder.period;
        this.revenue = builder.revenue;
        this.orders = builder.orders;
        this.products = builder.products;
        this.users = builder.users;
        this.brands = builder.brands;
        this.categories = builder.categories;
        this.revenueByDay = builder.revenueByDay;
        this.ordersByStatus = builder.ordersByStatus;
        this.salesByBrand = builder.salesByBrand;
        this.salesByCategory = builder.salesByCategory;
        this.topSellingProducts = builder.topSellingProducts;
        this.topRevenueProducts = builder.topRevenueProducts;
    }

    public PeriodInfo getPeriod() { return period; }
    public RevenueStats getRevenue() { return revenue; }
    public OrderStats getOrders() { return orders; }
    public ProductStats getProducts() { return products; }
    public UserStats getUsers() { return users; }
    public BrandStats getBrands() { return brands; }
    public CategoryStats getCategories() { return categories; }
    public Map<String, BigDecimal> getRevenueByDay() { return revenueByDay; }
    public Map<String, Integer> getOrdersByStatus() { return ordersByStatus; }
    public Map<String, Long> getSalesByBrand() { return salesByBrand; }
    public Map<String, Long> getSalesByCategory() { return salesByCategory; }
    public List<TopProductDTO> getTopSellingProducts() { return topSellingProducts; }
    public List<TopProductDTO> getTopRevenueProducts() { return topRevenueProducts; }

    public static class Builder {
        private PeriodInfo period;
        private RevenueStats revenue;
        private OrderStats orders;
        private ProductStats products;
        private UserStats users;
        private BrandStats brands;
        private CategoryStats categories;
        private Map<String, BigDecimal> revenueByDay;
        private Map<String, Integer> ordersByStatus;
        private Map<String, Long> salesByBrand;
        private Map<String, Long> salesByCategory;
        private List<TopProductDTO> topSellingProducts;
        private List<TopProductDTO> topRevenueProducts;

        public Builder period(PeriodInfo period) { this.period = period; return this; }
        public Builder revenue(RevenueStats revenue) { this.revenue = revenue; return this; }
        public Builder orders(OrderStats orders) { this.orders = orders; return this; }
        public Builder products(ProductStats products) { this.products = products; return this; }
        public Builder users(UserStats users) { this.users = users; return this; }
        public Builder brands(BrandStats brands) { this.brands = brands; return this; }
        public Builder categories(CategoryStats categories) { this.categories = categories; return this; }
        public Builder revenueByDay(Map<String, BigDecimal> revenueByDay) { this.revenueByDay = revenueByDay; return this; }
        public Builder ordersByStatus(Map<String, Integer> ordersByStatus) { this.ordersByStatus = ordersByStatus; return this; }
        public Builder salesByBrand(Map<String, Long> salesByBrand) { this.salesByBrand = salesByBrand; return this; }
        public Builder salesByCategory(Map<String, Long> salesByCategory) { this.salesByCategory = salesByCategory; return this; }
        public Builder topSellingProducts(List<TopProductDTO> topSellingProducts) { this.topSellingProducts = topSellingProducts; return this; }
        public Builder topRevenueProducts(List<TopProductDTO> topRevenueProducts) { this.topRevenueProducts = topRevenueProducts; return this; }
        public StatsReportDTO build() { return new StatsReportDTO(this); }
    }

    public static class PeriodInfo {
        private final String label;
        private final String from;
        private final String to;
        public PeriodInfo(String label, String from, String to) {
            this.label = label; this.from = from; this.to = to;
        }
        public String getLabel() { return label; }
        public String getFrom() { return from; }
        public String getTo() { return to; }
    }

    public static class RevenueStats {
        private final BigDecimal total;
        private final BigDecimal avgOrderValue;
        private final BigDecimal highestOrder;
        private final BigDecimal lowestOrder;
        public RevenueStats(BigDecimal total, BigDecimal avgOrderValue, BigDecimal highestOrder, BigDecimal lowestOrder) {
            this.total = total; this.avgOrderValue = avgOrderValue;
            this.highestOrder = highestOrder; this.lowestOrder = lowestOrder;
        }
        public BigDecimal getTotal() { return total; }
        public BigDecimal getAvgOrderValue() { return avgOrderValue; }
        public BigDecimal getHighestOrder() { return highestOrder; }
        public BigDecimal getLowestOrder() { return lowestOrder; }
    }

    public static class OrderStats {
        private final long total;
        private final long completed;
        private final long cancelled;
        private final long pending;
        private final long processing;
        private final long shipped;
        private final double completionRate;
        public OrderStats(long total, long completed, long cancelled, long pending, long processing, long shipped, double completionRate) {
            this.total = total; this.completed = completed; this.cancelled = cancelled;
            this.pending = pending; this.processing = processing; this.shipped = shipped;
            this.completionRate = completionRate;
        }
        public long getTotal() { return total; }
        public long getCompleted() { return completed; }
        public long getCancelled() { return cancelled; }
        public long getPending() { return pending; }
        public long getProcessing() { return processing; }
        public long getShipped() { return shipped; }
        public double getCompletionRate() { return completionRate; }
    }

    public static class ProductStats {
        private final long total;
        private final long active;
        private final long inactive;
        private final long lowStock;
        private final long outOfStock;
        private final double avgPrice;
        public ProductStats(long total, long active, long inactive, long lowStock, long outOfStock, double avgPrice) {
            this.total = total; this.active = active; this.inactive = inactive;
            this.lowStock = lowStock; this.outOfStock = outOfStock; this.avgPrice = avgPrice;
        }
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getInactive() { return inactive; }
        public long getLowStock() { return lowStock; }
        public long getOutOfStock() { return outOfStock; }
        public double getAvgPrice() { return avgPrice; }
    }

    public static class UserStats {
        private final long total;
        private final long active;
        private final long locked;
        private final long pending;
        private final long adminCount;
        private final long customerCount;
        public UserStats(long total, long active, long locked, long pending, long adminCount, long customerCount) {
            this.total = total; this.active = active; this.locked = locked;
            this.pending = pending; this.adminCount = adminCount; this.customerCount = customerCount;
        }
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getLocked() { return locked; }
        public long getPending() { return pending; }
        public long getAdminCount() { return adminCount; }
        public long getCustomerCount() { return customerCount; }
    }

    public static class BrandStats {
        private final long total;
        private final long withProducts;
        private final long withoutProducts;
        public BrandStats(long total, long withProducts, long withoutProducts) {
            this.total = total; this.withProducts = withProducts; this.withoutProducts = withoutProducts;
        }
        public long getTotal() { return total; }
        public long getWithProducts() { return withProducts; }
        public long getWithoutProducts() { return withoutProducts; }
    }

    public static class CategoryStats {
        private final long total;
        private final long withProducts;
        private final long withoutProducts;
        public CategoryStats(long total, long withProducts, long withoutProducts) {
            this.total = total; this.withProducts = withProducts; this.withoutProducts = withoutProducts;
        }
        public long getTotal() { return total; }
        public long getWithProducts() { return withProducts; }
        public long getWithoutProducts() { return withoutProducts; }
    }

    public static class TopProductDTO {
        private final String name;
        private final String thumbnailUrl;
        private final long quantitySold;
        private final BigDecimal totalRevenue;
        public TopProductDTO(String name, String thumbnailUrl, long quantitySold, BigDecimal totalRevenue) {
            this.name = name; this.thumbnailUrl = thumbnailUrl;
            this.quantitySold = quantitySold; this.totalRevenue = totalRevenue;
        }
        public String getName() { return name; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public long getQuantitySold() { return quantitySold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }
}
