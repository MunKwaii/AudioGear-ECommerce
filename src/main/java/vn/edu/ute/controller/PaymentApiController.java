package vn.edu.ute.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.impl.OrderServiceImpl;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.stream.StreamSupport;

@WebServlet("/api/v1/payment/check-status")
public class PaymentApiController extends HttpServlet {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(PaymentApiController.class);
    private final OrderService orderService = new OrderServiceImpl();
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String orderCode = req.getParameter("code");
        logger.info("Polling payment status for orderCode: {}", orderCode);

        if (orderCode == null || orderCode.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of("paid", false, "error", "Missing order code")));
            return;
        }

        try {
            Order order;
            try {
                order = orderService.getOrderByOrderCode(orderCode);
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(gson.toJson(Map.of("paid", false, "error", "Order not found")));
                return;
            }

            // Kiem tra trang thai cua don hang
            if (order.getStatus() != OrderStatus.PENDING) {
                // Return success if already processed and not canceled
                boolean isPaid = order.getStatus() == OrderStatus.PROCESSING ||
                        order.getStatus() == OrderStatus.SHIPPED ||
                        order.getStatus() == OrderStatus.DELIVERED;
                resp.getWriter().write(gson.toJson(Map.of("paid", isPaid)));
                return;
            }

            String apiToken = "HL1TIEXVOABXCTRDJFHOYNRJZULNZKC4IJTZQDSZM5B7NVQGSSPOY0W26MKEPWMU"; // SePay token
            String sepayUrl = "https://my.sepay.vn/userapi/transactions/list?limit=20";

            logger.info("Fetching latest 20 transactions from SePay: {}", sepayUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sepayUrl))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("SePay API returned status: {}", response.statusCode());

            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray transactions = jsonResponse.getAsJsonArray("transactions");

                if (transactions != null) {
                    // Sử dụng lambda và Stream API để tìm giao dịch khớp với orderCode (không phân
                    // biệt hoa thường)
                    final String searchCode = orderCode.toLowerCase();
                    final String searchCodeNoHyphen = searchCode.replace("-", "");

                    logger.info("Searching for code: {} or {}", searchCode, searchCodeNoHyphen);

                    Optional<JsonObject> matchedTransaction = StreamSupport.stream(transactions.spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .filter(tx -> {
                                JsonElement contentEl = tx.get("transaction_content");
                                if (contentEl == null || contentEl.isJsonNull())
                                    return false;
                                String content = contentEl.getAsString().toLowerCase();

                                // Kiểm tra chứa mã gốc hoặc mã đã bỏ dấu gạch ngang (vì ngân hàng hay xóa dấu
                                // -)
                                boolean match = content.contains(searchCode) || content.contains(searchCodeNoHyphen);

                                if (match) {
                                    logger.info("Match found for {}: content='{}'", orderCode, contentEl.getAsString());
                                }
                                return match;
                            })
                            .findFirst();

                    if (matchedTransaction.isPresent()) {
                        // Sử dụng OrderService để process đơn hàng (cập nhật trạng thái + gửi email)
                        orderService.processOrder(order.getId());

                        resp.getWriter().write(gson.toJson(Map.of("paid", true)));
                        return;
                    }
                }
            }

            resp.getWriter().write(gson.toJson(Map.of("paid", false)));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("paid", false, "error", e.getMessage())));
        }
    }
}
