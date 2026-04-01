package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.edu.ute.dto.response.ApiResponse;
import vn.edu.ute.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@WebServlet("/admin/products/upload-images")
@MultipartConfig
public class AdminProductImageUploadController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        List<String> stored = new ArrayList<>();
        String rootPath = System.getProperty("user.dir")
                + "/src/main/webapp/static/images/products";

        File root = new File(rootPath);
        if (!root.exists() && !root.mkdirs()) {
            ApiResponse response = new ApiResponse(false, "Khong tao duoc thu muc luu anh", null);
            resp.getWriter().write(JsonUtil.toJson(response));
            return;
        }

        for (Part part : req.getParts()) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }

            String submitted = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            if (!isImageFile(submitted)) {
                continue;
            }

            String extension = getExtension(submitted);
            String filename = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
            File output = new File(root, filename);
            part.write(output.getAbsolutePath());
            stored.add("/static/images/products/" + filename);
        }

        ApiResponse response = new ApiResponse(true, "OK", stored);
        resp.getWriter().write(JsonUtil.toJson(response));
    }

    private boolean isImageFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }

    private String getExtension(String name) {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
