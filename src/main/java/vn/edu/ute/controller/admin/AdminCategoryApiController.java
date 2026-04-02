package vn.edu.ute.controller.admin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.CategoryDTO;
import vn.edu.ute.service.CategoryService;
import vn.edu.ute.service.impl.CategoryServiceImpl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/categories/*")
public class AdminCategoryApiController extends HttpServlet {

    private final CategoryService categoryService = new CategoryServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<CategoryDTO> tree = categoryService.getAllCategoriesAsTree();
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", tree);
                resp.getWriter().write(gson.toJson(result));
            } else {
                Long id = extractId(pathInfo);
                if (id == null) {
                    sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                CategoryDTO dto = categoryService.getCategoryById(id);
                if (dto == null) {
                    sendError(resp, "Không tìm thấy danh mục", HttpServletResponse.SC_NOT_FOUND);
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
            String name = body.get("name").getAsString();
            String description = body.has("description") ? body.get("description").getAsString() : null;
            Long parentId = body.has("parentId") && !body.get("parentId").isJsonNull()
                    ? body.get("parentId").getAsLong() : null;

            CategoryDTO created = categoryService.createCategory(name, description, parentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tạo danh mục thành công");
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
            sendError(resp, "Thiếu ID danh mục", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            JsonObject body = parseRequestBody(req);
            String name = body.get("name").getAsString();
            String description = body.has("description") ? body.get("description").getAsString() : null;
            Long parentId = body.has("parentId") && !body.get("parentId").isJsonNull()
                    ? body.get("parentId").getAsLong() : null;

            CategoryDTO updated = categoryService.updateCategory(id, name, description, parentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Cập nhật danh mục thành công");
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
            sendError(resp, "Thiếu ID danh mục", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long id = extractId(pathInfo);
        if (id == null) {
            sendError(resp, "ID không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            categoryService.deleteCategory(id);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Xóa danh mục thành công");
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
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        resp.getWriter().write(gson.toJson(result));
    }
}
