package vn.edu.ute.service.stats.impl;

import vn.edu.ute.dao.BrandDao;
import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.BrandDaoImpl;
import vn.edu.ute.dao.impl.OrderDaoImpl;
import vn.edu.ute.dao.impl.ProductDaoImpl;
import vn.edu.ute.dao.impl.UserDAOImpl;
import vn.edu.ute.entity.Inventory;
import vn.edu.ute.homepage.factory.DaoFactory;
import vn.edu.ute.dto.StatsReportDTO;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.entity.enums.UserRole;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.service.stats.StatsReportService;
import vn.edu.ute.service.stats.TimeRangeStrategy;
import vn.edu.ute.service.stats.TimeRangeStrategyFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Facade Pattern: Tập hợp dữ liệu từ nhiều nguồn (Order, Product, User, Brand, Category)
 * để tạo ra một báo cáo thống kê toàn diện.
 */
public class StatsReportServiceImpl implements StatsReportService {

    private final OrderDao orderDao = new OrderDaoImpl();
    private final ProductDao productDao = ProductDaoImpl.getInstance();
    private final UserDAO userDAO = new UserDAOImpl();
    private final BrandDao brandDao = BrandDaoImpl.getInstance();

    @Override
    public StatsReportDTO generateReport(String timeRangeCode) {
        TimeRangeStrategy range = TimeRangeStrategyFactory.fromCode(timeRangeCode);
        LocalDateTime from = range.getFrom();
        LocalDateTime to = range.getTo();

        List<Order> allOrders = orderDao.findAllWithItems();
        List<Order> filteredOrders = allOrders.stream()
                .filter(o -> !o.getCreatedAt().isBefore(from) && !o.getCreatedAt().isAfter(to))
                .toList();

        List<Product> allProducts = productDao.searchProductsForAdmin(null, null, null, 0, 100000);
        List<User> allUsers = userDAO.findAll();
        List<Brand> allBrands = brandDao.findAll();

        StatsReportDTO.PeriodInfo period = new StatsReportDTO.PeriodInfo(
                range.getLabel(),
                from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        return new StatsReportDTO.Builder()
                .period(period)
                .revenue(buildRevenueStats(filteredOrders))
                .orders(buildOrderStats(filteredOrders))
                .products(buildProductStats(allProducts))
                .users(buildUserStats(allUsers))
                .brands(buildBrandStats(allBrands))
                .categories(buildCategoryStats(allProducts))
                .revenueByDay(buildRevenueByDay(filteredOrders, from, to))
                .ordersByStatus(buildOrdersByStatus(filteredOrders))
                .salesByBrand(buildSalesByBrand(filteredOrders))
                .salesByCategory(buildSalesByCategory(filteredOrders))
                .topSellingProducts(buildTopSellingProducts(filteredOrders, 10))
                .topRevenueProducts(buildTopRevenueProducts(filteredOrders, 10))
                .build();
    }

    private StatsReportDTO.RevenueStats buildRevenueStats(List<Order> orders) {
        List<Order> nonCancelled = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .toList();

        BigDecimal total = nonCancelled.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = nonCancelled.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(nonCancelled.size()), 0, RoundingMode.HALF_UP);

        BigDecimal highest = nonCancelled.stream()
                .map(Order::getTotalAmount)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal lowest = nonCancelled.stream()
                .map(Order::getTotalAmount)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        return new StatsReportDTO.RevenueStats(total, avg, highest, lowest);
    }

    private StatsReportDTO.OrderStats buildOrderStats(List<Order> orders) {
        long total = orders.size();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long pending = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long processing = orders.stream().filter(o -> o.getStatus() == OrderStatus.PROCESSING).count();
        long shipped = orders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPING).count();
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;

        return new StatsReportDTO.OrderStats(total, completed, cancelled, pending, processing, shipped, completionRate);
    }

    private StatsReportDTO.ProductStats buildProductStats(List<Product> products) {
        long total = products.size();
        long active = products.stream().filter(Product::getStatus).count();
        long inactive = total - active;
        long lowStock = products.stream().filter(p -> {
            int qty = DaoFactory.getInventoryDao().findByProductId(p.getId()).map(Inventory::getStockQuantity).orElse(0);
            return qty > 0 && qty < 10;
        }).count();
        long outOfStock = products.stream().filter(p -> {
            int qty = DaoFactory.getInventoryDao().findByProductId(p.getId()).map(Inventory::getStockQuantity).orElse(0);
            return qty <= 0;
        }).count();
        double avgPrice = products.isEmpty() ? 0 : products.stream()
                .mapToDouble(p -> p.getPrice().doubleValue()).average().orElse(0);

        return new StatsReportDTO.ProductStats(total, active, inactive, lowStock, outOfStock, avgPrice);
    }

    private StatsReportDTO.UserStats buildUserStats(List<User> users) {
        long total = users.size();
        long active = users.stream().filter(u -> u.getStatus() == UserStatus.active).count();
        long locked = users.stream().filter(u -> u.getStatus() == UserStatus.locked).count();
        long pending = users.stream().filter(u -> u.getStatus() == UserStatus.pending).count();
        long adminCount = users.stream().filter(u -> u.getRole() == UserRole.admin).count();
        long customerCount = users.stream().filter(u -> u.getRole() == UserRole.customer).count();

        return new StatsReportDTO.UserStats(total, active, locked, pending, adminCount, customerCount);
    }

    private StatsReportDTO.BrandStats buildBrandStats(List<Brand> brands) {
        long total = brands.size();
        long withProducts = brands.stream()
                .filter(b -> brandDao.countProductsByBrandId(b.getId()) > 0)
                .count();
        long withoutProducts = total - withProducts;
        return new StatsReportDTO.BrandStats(total, withProducts, withoutProducts);
    }

    private StatsReportDTO.CategoryStats buildCategoryStats(List<Product> products) {
        Set<Long> catIds = products.stream()
                .filter(p -> p.getCategory() != null)
                .map(p -> p.getCategory().getId())
                .collect(Collectors.toSet());
        long withProducts = catIds.size();

        long totalCategories = withProducts;

        return new StatsReportDTO.CategoryStats(totalCategories, withProducts, 0);
    }

    private Map<String, BigDecimal> buildRevenueByDay(List<Order> orders, LocalDateTime from, LocalDateTime to) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        
        // Aggregate by week/month for long periods to prevent UI lag
        long days = java.time.temporal.ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate());
        boolean aggregateByMonth = days > 90;
        
        DateTimeFormatter fmt = aggregateByMonth 
                ? DateTimeFormatter.ofPattern("MM/yy") 
                : DateTimeFormatter.ofPattern("dd/MM");

        LocalDate d = from.toLocalDate();
        while (!d.isAfter(to.toLocalDate())) {
            String key = d.format(fmt);
            result.putIfAbsent(key, BigDecimal.ZERO);
            d = aggregateByMonth ? d.plusMonths(1).withDayOfMonth(1) : d.plusDays(1);
        }

        orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .forEach(o -> {
                    String key = o.getCreatedAt().toLocalDate().format(fmt);
                    if (result.containsKey(key)) {
                        result.put(key, result.get(key).add(o.getTotalAmount()));
                    }
                });

        return result;
    }

    private Map<String, Integer> buildOrdersByStatus(List<Order> orders) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            result.put(s.name(), 0);
        }
        orders.forEach(o -> {
            String key = o.getStatus().name();
            result.put(key, result.getOrDefault(key, 0) + 1);
        });
        return result;
    }

    private Map<String, Long> buildSalesByBrand(List<Order> orders) {
        Map<String, Long> result = new LinkedHashMap<>();
        orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> {
                    String brandName = item.getProduct() != null && item.getProduct().getBrand() != null
                            ? item.getProduct().getBrand().getName() : "Không rõ";
                    long qty = item.getQuantity() != null ? item.getQuantity().longValue() : 0L;
                    result.merge(brandName, qty, Long::sum);
                });
        return result;
    }

    private Map<String, Long> buildSalesByCategory(List<Order> orders) {
        Map<String, Long> result = new LinkedHashMap<>();
        orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> {
                    String catName = item.getProduct() != null && item.getProduct().getCategory() != null
                            ? item.getProduct().getCategory().getName() : "Không rõ";
                    long qty = item.getQuantity() != null ? item.getQuantity().longValue() : 0L;
                    result.merge(catName, qty, Long::sum);
                });
        return result;
    }

    private List<StatsReportDTO.TopProductDTO> buildTopSellingProducts(List<Order> orders, int limit) {
        Map<Product, Long> salesMap = new HashMap<>();
        orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> salesMap.merge(item.getProduct(), item.getQuantity().longValue(), Long::sum));

        return salesMap.entrySet().stream()
                .sorted(Map.Entry.<Product, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new StatsReportDTO.TopProductDTO(
                        e.getKey().getName(),
                        e.getKey().getThumbnailUrl(),
                        e.getValue() != null ? e.getValue() : 0L,
                        BigDecimal.ZERO
                ))
                .toList();
    }

    private List<StatsReportDTO.TopProductDTO> buildTopRevenueProducts(List<Order> orders, int limit) {
        Map<Product, BigDecimal> revenueMap = new HashMap<>();
        orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .forEach(item -> {
                    BigDecimal itemRevenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    revenueMap.merge(item.getProduct(), itemRevenue, BigDecimal::add);
                });

        return revenueMap.entrySet().stream()
                .sorted(Map.Entry.<Product, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new StatsReportDTO.TopProductDTO(
                        e.getKey().getName(),
                        e.getKey().getThumbnailUrl(),
                        0,
                        e.getValue()
                ))
                .toList();
    }
}
