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
import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.dao.impl.OrderDaoImpl;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.enums.OrderStatus;

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

    private final OrderDao orderDao = new OrderDaoImpl();
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String orderCode = req.getParameter("code");

        if (orderCode == null || orderCode.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of("paid", false, "error", "Missing order code")));
            return;
        }

        try {
            Optional<Order> orderOpt = orderDao.findByOrderCode(orderCode);
            if (orderOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(gson.toJson(Map.of("paid", false, "error", "Order not found")));
                return;
            }

            Order order = orderOpt.get();

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
            String sepayUrl = "https://my.sepay.vn/userapi/transactions/list?since_id=20";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sepayUrl))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && response.body() != null) {
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                JsonArray transactions = jsonResponse.getAsJsonArray("transactions");

                if (transactions != null) {
                    // Sử dụng lambda và Stream API để tìm giao dịch khớp với orderCode
                    Optional<JsonObject> matchedTransaction = StreamSupport.stream(transactions.spliterator(), false)
                            .map(JsonElement::getAsJsonObject)
                            .filter(tx -> {
                                JsonElement contentEl = tx.get("transaction_content");
                                return contentEl != null && !contentEl.isJsonNull() 
                                       && contentEl.getAsString().contains(orderCode);
                            })
                            .findFirst();

                    if (matchedTransaction.isPresent()) {
                        order.setStatus(OrderStatus.PROCESSING);
                        orderDao.save(order);

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
