package vn.edu.ute.controller.admin;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.controller.BaseApiController;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserRole;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.service.UserService;
import vn.edu.ute.service.impl.UserServiceImpl;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/admin/users/*")
public class AdminUserApiController extends BaseApiController {

    private final UserService userService = new UserServiceImpl();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, () -> {
            String pathInfo = req.getPathInfo();
            System.out.println(">>> AdminUserApiController.doGet pathInfo: " + pathInfo);
            
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                handleListUsers(req, resp);
            } else if (pathInfo.equals("/stats")) {
                handleUserStats(resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleUserDetail(resp, pathInfo);
            } else {
                sendError(resp, "Endpoint không hợp lệ", HttpServletResponse.SC_NOT_FOUND);
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, () -> {
            JsonObject body = parseRequestBody(req);
            String email = getRequiredString(body, "email");
            String username = getOptionalString(body, "username");
            String fullName = getRequiredString(body, "fullName");
            String password = getRequiredString(body, "password");
            String phoneNumber = getOptionalString(body, "phoneNumber");
            UserRole role = parseRole(getOptionalString(body, "role"));
            UserStatus status = parseStatus(getOptionalString(body, "status"));

            User created = userService.createUser(email, username, fullName, password, phoneNumber, role, status);
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            sendSuccess(resp, "Tạo người dùng thành công", toUserResponse(created));
        });
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, () -> {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, "Thiếu ID người dùng", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (pathInfo.matches("/\\d+/status")) {
                handleUpdateStatus(req, resp, pathInfo);
            } else if (pathInfo.matches("/\\d+/role")) {
                handleUpdateRole(req, resp, pathInfo);
            } else if (pathInfo.matches("/\\d+")) {
                handleUpdateUser(req, resp, pathInfo);
            } else {
                sendError(resp, "Endpoint không hợp lệ", HttpServletResponse.SC_NOT_FOUND);
            }
        });
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, () -> {
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

            User user = userService.getUserById(id);
            if (user.getStatus() == UserStatus.locked) {
                userService.activateUser(user.getEmail());
                sendSuccess(resp, "Đã mở khóa người dùng", null);
            } else {
                userService.lockUser(id);
                sendSuccess(resp, "Đã khóa người dùng", null);
            }
        });
    }

    private void handleListUsers(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String keyword = req.getParameter("keyword");
        UserRole role = parseRole(req.getParameter("role"));
        UserStatus status = parseStatus(req.getParameter("status"));
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);
        if (page < 1) page = 1;
        if (size < 5) size = 5;

        long totalItems = userService.countSearchUsers(keyword, role, status);
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int offset = (page - 1) * size;

        List<User> users = userService.searchUsers(keyword, role, status, offset, size);
        List<Map<String, Object>> data = users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        result.put("total", totalItems);
        result.put("totalPages", totalPages);
        result.put("page", page);
        result.put("size", size);
        resp.getWriter().write(gson.toJson(result));
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void handleUserStats(HttpServletResponse resp) throws Exception {
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

        sendSuccess(resp, null, stats);
    }

    private void handleUserDetail(HttpServletResponse resp, String pathInfo) throws Exception {
        Long id = Long.parseLong(pathInfo.substring(1));
        User user = userService.getUserById(id);
        sendSuccess(resp, null, toUserResponse(user));
    }

    private void handleUpdateStatus(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws Exception {
        Long id = Long.parseLong(pathInfo.substring(1, pathInfo.lastIndexOf("/")));
        JsonObject body = parseRequestBody(req);
        UserStatus status = parseStatus(getRequiredString(body, "status"));
        
        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id) && status == UserStatus.locked) {
            sendError(resp, "Không thể khóa tài khoản của chính bạn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User updated = userService.updateUserStatus(id, status);
        sendSuccess(resp, "Cập nhật trạng thái thành công", toUserResponse(updated));
    }

    private void handleUpdateRole(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws Exception {
        Long id = Long.parseLong(pathInfo.substring(1, pathInfo.lastIndexOf("/")));
        JsonObject body = parseRequestBody(req);
        UserRole role = parseRole(getRequiredString(body, "role"));

        User updated = userService.updateUserRole(id, role);
        sendSuccess(resp, "Cập nhật quyền thành công", toUserResponse(updated));
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws Exception {
        Long id = Long.parseLong(pathInfo.substring(1));
        JsonObject body = parseRequestBody(req);
        String email = getOptionalString(body, "email");
        String username = getOptionalString(body, "username");
        String fullName = getOptionalString(body, "fullName");
        String password = getOptionalString(body, "password");
        String phoneNumber = getOptionalString(body, "phoneNumber");
        UserRole role = parseRole(getOptionalString(body, "role"));
        UserStatus status = parseStatus(getOptionalString(body, "status"));

        Long currentUserId = (Long) req.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id) && status == UserStatus.locked) {
            sendError(resp, "Không thể khóa tài khoản của chính bạn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User updated = userService.updateUser(id, email, username, fullName, password, phoneNumber, role, status);
        sendSuccess(resp, "Cập nhật người dùng thành công", toUserResponse(updated));
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

    private UserRole parseRole(String role) {
        if (role == null || role.trim().isEmpty()) return null;
        try {
            return UserRole.valueOf(role.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role không hợp lệ. Giá trị hợp lệ: customer, admin");
        }
    }

    private UserStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return UserStatus.valueOf(status.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Giá trị hợp lệ: active, locked, pending");
        }
    }
}
