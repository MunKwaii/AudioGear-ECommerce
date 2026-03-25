package vn.edu.ute.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.ute.auth.AuthService;
import vn.edu.ute.auth.LoginType;
import vn.edu.ute.auth.adapter.GoogleProfile;
import vn.edu.ute.auth.strategy.AuthRequest;
import vn.edu.ute.entity.User;

import java.io.IOException;
import java.util.Collections;

/**
 * Controller xử lý Token trả về từ Google Identity (Giao diện Frontend)
 */
@WebServlet("/api/auth/google")
public class GoogleAuthController extends HttpServlet {

    // Thay thế bằng Client ID được cung cấp từ người dùng
    private static final String CLIENT_ID = "161916572109-5iu1i7ibo1ub02oqsd6f8h67aqn0n65c.apps.googleusercontent.com";
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        // Có thể inject từ DI container, ở đây ta khởi tạo trực tiếp
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idTokenString = req.getParameter("credential");

        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Google Credential Token");
            return;
        }

        try {
            // 1. Xác minh chữ ký Token gửi từ Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                Payload payload = idToken.getPayload();

                // 2. Trích xuất thông tin Google
                GoogleProfile profile = new GoogleProfile();
                profile.setGoogleId(payload.getSubject());
                profile.setEmail(payload.getEmail());
                profile.setName((String) payload.get("name"));
                profile.setGivenName((String) payload.get("given_name"));
                profile.setFamilyName((String) payload.get("family_name"));
                profile.setPictureUrl((String) payload.get("picture"));

                // 3. Đưa cho Lõi (Strategy + Adapter) để Login / Auto-Register
                AuthRequest authRequest = new AuthRequest();
                authRequest.setGoogleProfile(profile);

                User loggedInUser = authService.login(LoginType.GOOGLE, authRequest);

                // 4. Đăng nhập thành công, tạo JWT thay vì chỉ xài Session truyền thống
                vn.edu.ute.util.JwtUtil jwtUtil = new vn.edu.ute.util.JwtUtil();
                String token = jwtUtil.generateToken(loggedInUser.getId(), loggedInUser.getEmail(), loggedInUser.getRole().name());
                
                // Set Session fallback
                HttpSession session = req.getSession(true);
                session.setAttribute("loggedInUser", loggedInUser);

                // Gửi Token qua Cookie để giao diện/JS có thể lấy
                jakarta.servlet.http.Cookie tokenCookie = new jakarta.servlet.http.Cookie("accessToken", token);
                tokenCookie.setPath("/");
                tokenCookie.setMaxAge(24 * 60 * 60); // 24 hours
                resp.addCookie(tokenCookie);

                // Chuyển hướng người dùng về trang chủ
                resp.sendRedirect(req.getContextPath() + "/");
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid ID token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi xác thực: " + e.getMessage());
        }
    }
}
