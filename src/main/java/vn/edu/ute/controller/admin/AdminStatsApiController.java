package vn.edu.ute.controller.admin;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.OrderResponseDto;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.service.impl.OrderServiceImpl;
import vn.edu.ute.service.impl.ProductServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API cung cấp toàn bộ chỉ số thống kê cho Dashboard Admin.
 * Endpoint: /api/admin/stats
 */
@WebServlet("/api/admin/stats")
public class AdminStatsApiController extends HttpServlet {

    private final OrderService orderService = new OrderServiceImpl();
    private final ProductService productService = new ProductServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Lấy dữ liệu Đơn hàng
            List<Order> allOrders = orderService.getAllOrders();
            
            // 2. Tính toán doanh thu và đếm trạng thái
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Map<String, Integer> orderCounts = new HashMap<>();
            for (OrderStatus status : OrderStatus.values()) {
                orderCounts.put(status.name(), 0);
            }

            for (Order order : allOrders) {
                String status = order.getStatus().name();
                orderCounts.put(status, orderCounts.get(status) + 1);
                
                if (order.getStatus() != OrderStatus.CANCELLED) {
                    totalRevenue = totalRevenue.add(order.getTotalAmount());
                }
            }

            // 3. Tính xu hướng doanh thu 7 ngày (Trừ hôm nay là ngày cuối)
            Map<String, BigDecimal> revenueTrendMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                revenueTrendMap.put(date.format(formatter), BigDecimal.ZERO);
            }

            for (Order order : allOrders) {
                if (order.getStatus() != OrderStatus.CANCELLED) {
                    String dateKey = order.getCreatedAt().toLocalDate().format(formatter);
                    if (revenueTrendMap.containsKey(dateKey)) {
                        revenueTrendMap.put(dateKey, revenueTrendMap.get(dateKey).add(order.getTotalAmount()));
                    }
                }
            }

            List<Map<String, Object>> trendData = revenueTrendMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", e.getKey());
                    m.put("amount", e.getValue());
                    return m;
                }).collect(Collectors.toList());

            // 4. Lấy thống kê Sản phẩm & Người dùng
            long totalProducts = productService.countSearchProductsForAdmin("", null, null);
            
            // Lấy tạm số lượng tồn kho thấp (Giả định fetch toàn bộ để filter cho demo, tối ưu sau)
            // Lưu ý: Trong thực tế nên dùng query COUNT(*) WHERE stock < 10
            List<Product> products = productService.searchProductsForAdmin("", null, null, 0, 1000);
            long lowStockCount = products.stream().filter(p -> {
                int qty = p.getInventory() != null ? p.getInventory().getStockQuantity() : 0;
                return qty < 10;
            }).count();

            // 5. Kết quả gần đây
            List<Map<String, Object>> recentOrders = OrderResponseDto.fromEntities(
                allOrders.stream().limit(5).collect(Collectors.toList())
            );

            // 6. Tổng hợp Response
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("totalRevenue", totalRevenue);
            result.put("totalOrders", allOrders.size());
            result.put("orderCounts", orderCounts);
            result.put("totalProducts", totalProducts);
            result.put("lowStockCount", lowStockCount);
            result.put("recentOrders", recentOrders);
            result.put("revenueTrend", trendData);

            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("success", false, "message", e.getMessage())));
        }
    }
}
