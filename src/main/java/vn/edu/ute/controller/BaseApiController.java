package vn.edu.ute.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Controller áp dụng Template Method Pattern cho các REST API.
 */
public abstract class BaseApiController extends HttpServlet {

    protected final Gson gson = new Gson();

    @FunctionalInterface
    protected interface ApiHandler {
        void handle() throws Exception;
    }

    /**
     * Template Method: Xử lý quy trình chung của một API Request.
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp, ApiHandler handler) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            handler.handle();
        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Lỗi hệ thống: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void sendSuccess(HttpServletResponse resp, String message, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (message != null) result.put("message", message);
        if (data != null) result.put("data", data);
        resp.getWriter().write(gson.toJson(result));
    }

    protected void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }

    protected JsonObject parseRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String raw = sb.toString().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu yêu cầu không được để trống");
        }
        return JsonParser.parseString(raw).getAsJsonObject();
    }

    protected String getRequiredString(JsonObject body, String fieldName) {
        if (!body.has(fieldName) || body.get(fieldName).isJsonNull()) {
            throw new IllegalArgumentException("Thiếu trường bắt buộc: " + fieldName);
        }
        String value = body.get(fieldName).getAsString().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Trường bắt buộc không được để trống: " + fieldName);
        }
        return value;
    }

    protected String getOptionalString(JsonObject body, String fieldName) {
        if (!body.has(fieldName) || body.get(fieldName).isJsonNull()) {
            return null;
        }
        String value = body.get(fieldName).getAsString().trim();
        return value.isEmpty() ? null : value;
    }
}
