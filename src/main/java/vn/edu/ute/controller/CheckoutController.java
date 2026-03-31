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
import vn.edu.ute.security.CurrentUser;
import vn.edu.ute.security.JwtUserParser;
import vn.edu.ute.service.CheckoutService;
import vn.edu.ute.service.impl.CheckoutServiceImpl;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/v1/checkout")
public class CheckoutController extends HttpServlet {

    private final Gson gson = new Gson();
    private final CheckoutService checkoutService = new CheckoutServiceImpl();
    private final JwtUserParser jwtUserParser = new JwtUserParser();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            CheckoutRequest request = gson.fromJson(req.getReader(), CheckoutRequest.class);

            // 1. Lấy user từ JWT nếu request có Authorization header
            CurrentUser currentUser = jwtUserParser.parseFromRequest(req);

            // 2. Nếu chưa có JWT thì thử lấy từ request attribute / session
            if (currentUser == null) {
                Object userIdAttr = req.getAttribute("userId");
                Object emailAttr = req.getAttribute("email");
                Object roleAttr = req.getAttribute("role");

                if (userIdAttr instanceof Long) {
                    currentUser = new CurrentUser(
                            (Long) userIdAttr,
                            emailAttr != null ? emailAttr.toString() : null,
                            roleAttr != null ? roleAttr.toString() : null
                    );
                } else if (userIdAttr instanceof Integer) {
                    currentUser = new CurrentUser(
                            ((Integer) userIdAttr).longValue(),
                            emailAttr != null ? emailAttr.toString() : null,
                            roleAttr != null ? roleAttr.toString() : null
                    );
                }
            }

            // 3. Nếu muốn bắt buộc đăng nhập mới checkout thì mở đoạn này
            if (currentUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(gson.toJson(Map.of(
                        "success", false,
                        "message", "Bạn cần đăng nhập để thực hiện checkout"
                )));
                return;
            }

            CheckoutResponse response = checkoutService.checkout(currentUser.getUserId(), request);

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