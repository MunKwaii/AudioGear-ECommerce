package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import vn.edu.ute.util.storage.PathResolver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/api/admin/brands/upload-logo")
@MultipartConfig
public class AdminBrandLogoUploadController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Part part = req.getPart("logo");
        if (part == null || part.getSize() == 0) {
            sendError(resp, "Không có file nào được chọn", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String submitted = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        if (!isImageFile(submitted)) {
            sendError(resp, "Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String extension = getExtension(submitted);
        String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        vn.edu.ute.util.storage.StorageStrategy storageStrategy = new vn.edu.ute.util.storage.CloudinaryStorageStrategy();

        try {
            String url = storageStrategy.store(part, filename, "brands");
            resp.getWriter().write("{\"success\":true,\"url\":\"" + url + "\"}");
        } catch (Exception e) {
            sendError(resp, "Lỗi khi lưu ảnh: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private List<File> resolveRoots(String staticPath) {
        return PathResolver.resolveRoots(getServletContext(), staticPath);
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp");
    }

    private String getExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) return "";
        return name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void sendError(HttpServletResponse resp, String message, int status) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"success\":false,\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
