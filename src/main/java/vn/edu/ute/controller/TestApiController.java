package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Endpoint này dùng để test bộ lọc JWT.
 * Vì nó nằm trong /api/* và KHÔNG thuộc diện public,
 * trình duyệt cần phải có JWT Token trong Cookie hoặc Header mới được vào.
 */
@WebServlet("/api/test/me")
public class TestApiController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 1. JWTFilter đã xác thực thành công và nhúng dữ liệu vào Request
        String email = (String) req.getAttribute("currentUserEmail");
        Long userId = (Long) req.getAttribute("currentUserId");
        String role = (String) req.getAttribute("currentUserRole");

        // 2. Trả về JSON chứa thông tin giải mã từ Token
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        PrintWriter out = resp.getWriter();
        out.println("{");
        out.println("  \"success\": true,");
        out.println("  \"message\": \"Bạn đã vượt qua chốt chặn bảo mật JWT thành công!\",");
        out.println("  \"data\": {");
        out.println("    \"userId\": " + userId + ",");
        out.println("    \"email\": \"" + email + "\",");
        out.println("    \"role\": \"" + role + "\"");
        out.println("  }");
        out.println("}");
    }
}
