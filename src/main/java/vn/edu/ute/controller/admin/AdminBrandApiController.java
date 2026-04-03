package vn.edu.ute.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.BrandDTO;
import vn.edu.ute.service.BrandService;
import vn.edu.ute.service.impl.BrandServiceImpl;

import java.io.IOException;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/brands/*")
public class AdminBrandApiController extends HttpServlet {

    private final BrandService brandService = new BrandServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<BrandDTO> brands = brandService.getAllBrands();
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", brands);
                resp.getWriter().write(gson.toJson(result));
            } else {
                Long id = extractId(pathInfo);
                if (id == null) {
                    sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                BrandDTO dto = brandService.getBrandById(id);
                if (dto == null) {
                    sendError(resp, "Không tìm thấy thương hiệu", HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", dto);
                resp.getWriter().write(gson.toJson(result));
            }
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
            String name = getRequiredString(body, "name");
            String description = getOptionalString(body, "description");
            String logoUrl = getOptionalString(body, "logoUrl");

            BrandDTO created = brandService.createBrand(name, description, logoUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tạo thương hiệu thành công");
            result.put("data", created);
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
            sendError(resp, "Thiếu ID thương hiệu", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            JsonObject body = parseRequestBody(req);
            String name = getRequiredString(body, "name");
            String description = getOptionalString(body, "description");
            String logoUrl = getOptionalString(body, "logoUrl");

            BrandDTO updated = brandService.updateBrand(id, name, description, logoUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cập nhật thương hiệu thành công");
            result.put("data", updated);
            resp.getWriter().write(gson.toJson(result));

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
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(resp, "Thiếu ID thương hiệu", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            brandService.deleteBrand(id);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Xóa thương hiệu thành công");
            resp.getWriter().write(gson.toJson(result));

        } catch (IllegalArgumentException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (Exception e) {
            sendError(resp, e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Long extractId(String pathInfo) {
        try {
            String[] parts = pathInfo.split("/");
            if (parts.length > 1) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
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
            throw new IllegalArgumentException("Tên thương hiệu không được để trống");
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

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }
}
