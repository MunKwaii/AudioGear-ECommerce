package vn.edu.ute.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserRole;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.service.UserService;
import vn.edu.ute.service.impl.UserServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/admin/users/*")
public class AdminUserApiController extends HttpServlet {

    private final UserService userService = new UserServiceImpl();
    private final Gson gson = new Gson();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            System.out.println(">>> AdminUserApiController.doGet pathInfo: " + pathInfo);
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListUsers(req, resp);
            } else if (pathInfo.equals("/stats")) {
                handleUserStats(resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleUserDetail(resp, pathInfo);
            } else {
                System.err.println(">>> Unknown endpoint: " + pathInfo);
                sendError(resp, "Endpoint không hợp lệ", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            JsonObject body = parseRequestBody(req);
            String email = getRequiredString(body, "email");
            String username = getOptionalString(body, "username");
            String fullName = getRequiredString(body, "fullName");
            String password = getRequiredString(body, "password");
            String phoneNumber = getOptionalString(body, "phoneNumber");
            UserRole role = parseRole(getOptionalString(body, "role"));
            UserStatus status = parseStatus(getOptionalString(body, "status"));

            User created = userService.createUser(email, username, fullName, password, phoneNumber, role, status);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tạo người dùng thành công");
            result.put("data", toUserResponse(created));
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(result));
        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, "Thiếu ID người dùng", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if (pathInfo.matches("/\\d+/status")) {
                handleUpdateStatus(req, resp, pathInfo);
                return;
            }
            if (pathInfo.matches("/\\d+/role")) {
                handleUpdateRole(req, resp, pathInfo);
                return;
            }
            if (pathInfo.matches("/\\d+")) {
                handleUpdateUser(req, resp, pathInfo);
                return;
            }
            sendError(resp, "Endpoint không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            sendError(resp, "Thiếu ID người dùng", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = Long.parseLong(pathInfo.substring(1));
        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            sendError(resp, "Không thể khóa tài khoản của chính bạn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            userService.lockUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã khóa người dùng");
            resp.getWriter().write(gson.toJson(result));
        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleListUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String keyword = req.getParameter("keyword");
        UserRole role = parseRole(req.getParameter("role"));
        UserStatus status = parseStatus(req.getParameter("status"));

        List<User> users = userService.searchUsers(keyword, role, status);
        List<Map<String, Object>> data = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("total", data.size());
        resp.getWriter().write(gson.toJson(result));
    }

    private void handleUserStats(HttpServletResponse resp) throws IOException {
        List<User> users = userService.searchUsers(null, null, null);
        long total = users.size();
        long active = users.stream().filter(u -> u.getStatus() == UserStatus.active).count();
        long locked = users.stream().filter(u -> u.getStatus() == UserStatus.locked).count();
        long pending = users.stream().filter(u -> u.getStatus() == UserStatus.pending).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("locked", locked);
        stats.put("pending", pending);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        resp.getWriter().write(gson.toJson(result));
    }

    private void handleUserDetail(HttpServletResponse resp, String pathInfo) throws IOException {
        Long id = Long.parseLong(pathInfo.substring(1));
        User user = userService.getUserById(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", toUserResponse(user));
        resp.getWriter().write(gson.toJson(result));
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        Long id = Long.parseLong(pathInfo.substring(1, pathInfo.lastIndexOf("/")));
        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            sendError(resp, "Không thể thay đổi trạng thái của chính bạn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        JsonObject body = parseRequestBody(req);
        UserStatus status = parseStatus(getRequiredString(body, "status"));

        User updated = userService.updateUserStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Cập nhật trạng thái thành công");
        result.put("data", toUserResponse(updated));
        resp.getWriter().write(gson.toJson(result));
    }

    private void handleUpdateRole(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        Long id = Long.parseLong(pathInfo.substring(1, pathInfo.lastIndexOf("/")));
        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            sendError(resp, "Không thể thay đổi quyền của chính bạn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        JsonObject body = parseRequestBody(req);
        UserRole role = parseRole(getRequiredString(body, "role"));

        User updated = userService.updateUserRole(id, role);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Cập nhật quyền thành công");
        result.put("data", toUserResponse(updated));
        resp.getWriter().write(gson.toJson(result));
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        Long id = Long.parseLong(pathInfo.substring(1));
        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            sendError(resp, "Không thể sửa tài khoản của chính bạn tại đây", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject body = parseRequestBody(req);
        String email = getOptionalString(body, "email");
        String username = getOptionalString(body, "username");
        String fullName = getOptionalString(body, "fullName");
        String password = getOptionalString(body, "password");
        String phoneNumber = getOptionalString(body, "phoneNumber");
        UserRole role = parseRole(getOptionalString(body, "role"));
        UserStatus status = parseStatus(getOptionalString(body, "status"));

        User updated = userService.updateUser(id, email, username, fullName, password, phoneNumber, role, status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Cập nhật người dùng thành công");
        result.put("data", toUserResponse(updated));
        resp.getWriter().write(gson.toJson(result));
    }

    private Map<String, Object> toUserResponse(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("phoneNumber", user.getPhoneNumber());
        data.put("role", user.getRole() != null ? user.getRole().name() : null);
        data.put("status", user.getStatus() != null ? user.getStatus().name() : null);
        data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null);
        data.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().format(FORMATTER) : null);
        return data;
    }

    private JsonObject parseRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String raw = sb.toString().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    private String getRequiredString(JsonObject body, String fieldName) {
        if (!body.has(fieldName) || body.get(fieldName).isJsonNull()) {
            throw new IllegalArgumentException("Thiếu trường bắt buộc: " + fieldName);
        }
        String value = body.get(fieldName).getAsString().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Trường bắt buộc không được để trống: " + fieldName);
        }
        return value;
    }

    private String getOptionalString(JsonObject body, String fieldName) {
        if (!body.has(fieldName) || body.get(fieldName).isJsonNull()) {
            return null;
        }
        String value = body.get(fieldName).getAsString().trim();
        return value.isEmpty() ? null : value;
    }

    private UserRole parseRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        try {
            return UserRole.valueOf(role.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role không hợp lệ. Giá trị hợp lệ: customer, admin");
        }
    }

    private UserStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return UserStatus.valueOf(status.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Giá trị hợp lệ: active, locked, pending");
        }
    }

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }
}

