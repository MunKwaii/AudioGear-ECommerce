package vn.edu.ute.controller.admin;

import com.google.gson.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.entity.Voucher;
import vn.edu.ute.entity.enums.VoucherStatus;
import vn.edu.ute.service.VoucherService;
import vn.edu.ute.service.impl.VoucherServiceImpl;

import vn.edu.ute.dto.VoucherDTO;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet API for Voucher CRUD operations for Admin.
 * Handles AJAX requests from the management page.
 */
@WebServlet("/api/admin/vouchers/*")
public class AdminVoucherApiController extends HttpServlet {

    private final VoucherService voucherService = new VoucherServiceImpl();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                    LocalDateTime.parse(json.getAsString()))
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // List search
                String keyword = req.getParameter("keyword");
                String statusStr = req.getParameter("status");
                VoucherStatus status = (statusStr != null && !statusStr.isEmpty()) ? VoucherStatus.valueOf(statusStr) : null;
                int page = Integer.parseInt(req.getParameter("page") != null ? req.getParameter("page") : "1");
                int size = Integer.parseInt(req.getParameter("size") != null ? req.getParameter("size") : "10");

                List<VoucherDTO> vouchers = voucherService.searchVouchers(keyword, status, page, size);
                long total = voucherService.countSearch(keyword, status);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("vouchers", vouchers);
                result.put("total", total);
                result.put("page", page);
                result.put("size", size);
                result.put("totalPages", (int) Math.ceil((double) total / size));
                resp.getWriter().write(gson.toJson(result));
            } else {
                // Get single
                Long id = extractId(pathInfo);
                voucherService.getVoucherById(id).ifPresentOrElse(v -> {
                    try {
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", true);
                        result.put("voucher", v);
                        resp.getWriter().write(gson.toJson(result));
                    } catch (IOException ignored) {}
                }, () -> {
                    try { sendError(resp, "Voucher không tồn tại", 404); } catch (IOException ignored) {}
                });
            }
        } catch (Exception e) {
            sendError(resp, e.getMessage(), 500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            Voucher voucher = parseRequestBody(req);
            Voucher created = voucherService.createVoucher(voucher);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tạo voucher thành công");
            result.put("voucher", created);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            sendError(resp, e.getMessage(), 400);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", 400);
            return;
        }

        try {
            Voucher voucher = parseRequestBody(req);
            voucher.setId(id);
            Voucher updated = voucherService.updateVoucher(voucher);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cập nhật voucher thành công");
            result.put("voucher", updated);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            sendError(resp, e.getMessage(), 400);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String pathInfo = req.getPathInfo();
        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", 400);
            return;
        }

        try {
            voucherService.deleteVoucher(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Xóa voucher thành công");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            sendError(resp, e.getMessage(), 500);
        }
    }

    private Long extractId(String pathInfo) {
        try {
            String[] parts = pathInfo.split("/");
            if (parts.length > 1) return Long.parseLong(parts[1]);
        } catch (Exception ignored) {}
        return null;
    }

    private Voucher parseRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return gson.fromJson(sb.toString(), Voucher.class);
    }

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }
}
