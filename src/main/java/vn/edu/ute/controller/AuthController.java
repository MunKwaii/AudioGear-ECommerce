package vn.edu.ute.controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.dto.request.RegisterRequest;
import vn.edu.ute.dto.request.ResetPasswordRequest;
import vn.edu.ute.dto.request.VerifyOtpRequest;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.facade.AuthFacade;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthController extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AuthController.class);
    private final Gson gson = new Gson();
    private AuthFacade authFacade;

    @Override
    public void init() throws ServletException {
        // Khởi tạo Facade, nơi chứa toàn bộ logic Auth
        this.authFacade = new AuthFacade();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null) {
                sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, false, "Invalid path", null);
                return;
            }

            switch (pathInfo) {
                // ==========================================
                // LUỒNG ĐĂNG KÝ (SIGNUP)
                // ==========================================
                case "/register":
                    handleRegisterRequestOtp(req, resp);
                    break;
                case "/register/verify":
                    handleRegisterVerify(req, resp);
                    break;

                // ==========================================
                // LUỒNG QUÊN MẬT KHẨU (FORGOT PASSWORD)
                // ==========================================
                case "/forgot-password/request":
                    handleForgotPasswordRequest(req, resp);
                    break;
                case "/forgot-password/verify":
                    handleForgotPasswordVerify(req, resp);
                    break;
                case "/forgot-password/reset":
                    handleResetPassword(req, resp);
                    break;
                default:
                    sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, false, "Endpoint not found", null);
            }
        } catch (RuntimeException e) {
            // Các Exception được ném ra từ AuthFacade (như sai OTP, trùng email...)
            logger.warn("Business Logic error: {}", e.getMessage());
            sendResponse(resp, HttpServletResponse.SC_BAD_REQUEST, false, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("System error in AuthController", e);
            sendResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, false, "Lỗi hệ thống không xác định", null);
        }
    }

    // --- Các hàm xử lý Router chi tiết ---

    private void handleRegisterRequestOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterRequest body = parseBody(req, RegisterRequest.class);
        authFacade.requestRegister(body);
        sendResponse(resp, HttpServletResponse.SC_OK, true, "Hệ thống đã gửi OTP đến email của bạn. Vui lòng xác nhận!", null);
    }

    private void handleRegisterVerify(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        VerifyOtpRequest body = parseBody(req, VerifyOtpRequest.class);
        authFacade.verifyRegister(body.getEmail(), body.getOtp());
        sendResponse(resp, HttpServletResponse.SC_OK, true, "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.", null);
    }

    private void handleForgotPasswordRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        VerifyOtpRequest body = parseBody(req, VerifyOtpRequest.class);
        authFacade.requestForgotPassword(body.getEmail());
        sendResponse(resp, HttpServletResponse.SC_OK, true, "Mã khôi phục đã được gửi tới email của bạn (5 phút).", null);
    }

    private void handleForgotPasswordVerify(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        VerifyOtpRequest body = parseBody(req, VerifyOtpRequest.class);
        authFacade.verifyForgotPassword(body.getEmail(), body.getOtp());
        sendResponse(resp, HttpServletResponse.SC_OK, true, "Mã xác minh hợp lệ. Hãy tạo mật khẩu mới.", null);
    }

    private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ResetPasswordRequest body = parseBody(req, ResetPasswordRequest.class);
        authFacade.resetPassword(body.getEmail(), body.getOtp(), body.getNewPassword());
        sendResponse(resp, HttpServletResponse.SC_OK, true, "Mật khẩu đã được thiết lập lại thành công. Bạn có thể đăng nhập.", null);
    }

    // --- Utils parse JSON và trả về Response ---

    private <T> T parseBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return gson.fromJson(sb.toString(), clazz);
    }

    private void sendResponse(HttpServletResponse response, int statusCode, boolean success, String message, Object data) throws IOException {
        response.setStatus(statusCode);
        ApiResponse apiResponse = new ApiResponse(success, message, data);
        response.getWriter().write(gson.toJson(apiResponse));
    }
}
