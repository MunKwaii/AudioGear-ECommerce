package vn.edu.ute.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.request.CheckoutRequest;
import vn.edu.ute.dto.response.CheckoutResponse;
import vn.edu.ute.exception.VoucherException;
import vn.edu.ute.service.CheckoutService;
import vn.edu.ute.service.impl.CheckoutServiceImpl;

import java.io.IOException;
import java.util.Map;

/**
 * Controller xử lý checkout.
 * CheckoutAuthFilter chạy trước đã đảm bảo user đăng nhập (currentUserId != null).
 */
@WebServlet("/api/v1/checkout")
public class CheckoutController extends HttpServlet {

    private final Gson gson = new Gson();
    private final CheckoutService checkoutService = new CheckoutServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            CheckoutRequest request = gson.fromJson(req.getReader(), CheckoutRequest.class);

            // CheckoutAuthFilter đã validate auth → currentUserId luôn tồn tại
            Long userId = (Long) req.getAttribute("currentUserId");

            CheckoutResponse response = checkoutService.checkout(userId, request);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(response));

        } catch (VoucherException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", e.getMessage()
            )));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", e.getMessage()
            )));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of(
                    "success", false,
                    "message", "Lỗi hệ thống khi checkout"
            )));
        }
    }
}