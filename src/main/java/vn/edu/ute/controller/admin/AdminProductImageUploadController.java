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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        String staticPath = "/static/images/products";
        List<File> roots = new ArrayList<>();

        // 1. Get deployment path (target)
        String deployPath = getServletContext().getRealPath(staticPath);
        if (deployPath != null) {
            File deployRoot = new File(deployPath);
            roots.add(deployRoot);

            // 2. Try to find 'src' by looking for 'target' folder and going up
            File current = deployRoot;
            while (current != null) {
                if (current.getName().equals("target")) {
                    File projectRoot = current.getParentFile();
                    if (projectRoot != null) {
                        File srcRoot = new File(projectRoot, "src/main/webapp" + staticPath.replace("/", File.separator));
                        if (!roots.contains(srcRoot)) {
                            // Use index 0 to prioritize saving to src
                            roots.add(0, srcRoot);
                        }
                    }
                    break;
                }
                current = current.getParentFile();
            }
        }

        // 3. Fallback: Check user.dir if src was not found via target
        if (roots.size() < 2) {
            String userDir = System.getProperty("user.dir");
            File projectRoot = new File(userDir);
            // Search upwards for pom.xml or src
            while (projectRoot != null && !new File(projectRoot, "pom.xml").exists() && !new File(projectRoot, "src").exists()) {
                projectRoot = projectRoot.getParentFile();
            }

            if (projectRoot != null) {
                File srcRoot = new File(projectRoot, "src/main/webapp" + staticPath.replace("/", File.separator));
                if (!roots.contains(srcRoot)) {
                    roots.add(0, srcRoot);
                }
            }
        }

        if (roots.isEmpty()) {
            ApiResponse response = new ApiResponse(false, "Không xác định được thư mục lưu ảnh", null);
            resp.getWriter().write(JsonUtil.toJson(response));
            return;
        }

        for (File root : roots) {
            if (!root.exists() && !root.mkdirs()) {
                ApiResponse response = new ApiResponse(false, "Không tạo được thư mục: " + root.getAbsolutePath(), null);
                resp.getWriter().write(JsonUtil.toJson(response));
                return;
            }
        }

        try {
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

                // Write to the first root directory
                File firstOutput = new File(roots.get(0), filename);
                part.write(firstOutput.getAbsolutePath());

                // Copy from the first output to other roots (if any)
                for (int i = 1; i < roots.size(); i++) {
                    File otherOutput = new File(roots.get(i), filename);
                    Files.copy(firstOutput.toPath(), otherOutput.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                stored.add(staticPath + "/" + filename);
            }
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(false, "Lỗi khi lưu ảnh: " + e.getMessage(), null);
            resp.getWriter().write(JsonUtil.toJson(response));
            return;
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
